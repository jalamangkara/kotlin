/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan.descriptors

import org.jetbrains.kotlin.backend.konan.*
import org.jetbrains.kotlin.backend.konan.ir.getSuperClassNotAny
import org.jetbrains.kotlin.backend.konan.ir.getSuperInterfaces
import org.jetbrains.kotlin.backend.konan.ir.isNothing
import org.jetbrains.kotlin.backend.konan.llvm.isVoidAsReturnType
import org.jetbrains.kotlin.backend.konan.lower.erasedUpperBound
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.IrClassifierSymbol
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*

/**
 * List of all implemented interfaces (including those which implemented by a super class)
 */
internal val IrClass.implementedInterfaces: List<IrClass>
    get() {
        val superClassImplementedInterfaces = this.getSuperClassNotAny()?.implementedInterfaces ?: emptyList()
        val superInterfaces = this.getSuperInterfaces()
        val superInterfacesImplementedInterfaces = superInterfaces.flatMap { it.implementedInterfaces }
        return (superClassImplementedInterfaces +
                superInterfacesImplementedInterfaces +
                superInterfaces).distinct()
    }

internal val IrFunction.isTypedIntrinsic: Boolean
    get() = annotations.hasAnnotation(KonanFqNames.typedIntrinsic)

internal val IrConstructor.isConstantConstructorIntrinsic: Boolean
    get() = annotations.hasAnnotation(KonanFqNames.constantConstructorIntrinsic)

internal val arrayTypes = setOf(
        "kotlin.Array",
        "kotlin.ByteArray",
        "kotlin.CharArray",
        "kotlin.ShortArray",
        "kotlin.IntArray",
        "kotlin.LongArray",
        "kotlin.FloatArray",
        "kotlin.DoubleArray",
        "kotlin.BooleanArray",
        "kotlin.native.ImmutableBlob",
        "kotlin.native.internal.NativePtrArray"
)

internal val arraysWithFixedSizeItems = setOf(
        "kotlin.ByteArray",
        "kotlin.CharArray",
        "kotlin.ShortArray",
        "kotlin.IntArray",
        "kotlin.LongArray",
        "kotlin.FloatArray",
        "kotlin.DoubleArray",
        "kotlin.BooleanArray"
)

internal val IrClass.isArray: Boolean
    get() = this.fqNameForIrSerialization.asString() in arrayTypes

internal val IrClass.isArrayWithFixedSizeItems: Boolean
    get() = this.fqNameForIrSerialization.asString() in arraysWithFixedSizeItems

fun IrClass.isAbstract() = this.modality == Modality.SEALED || this.modality == Modality.ABSTRACT

private enum class TypeKind {
    NOTHING,
    UNIT,
    VALUE_TYPE,
    REFERENCE,
    GENERIC
}

private data class TypeWithKind(val erasedType: IrType?, val kind: TypeKind) {
    companion object {
        fun fromType(irType: IrType?): TypeWithKind {
            val erasedUpperBound = irType?.erasedUpperBound
            val erasedType = erasedUpperBound?.defaultType?.let {
                if (irType.isNullable()) it.makeNullable() else it
            }
            return when {
                irType == null -> TypeWithKind(null, TypeKind.NOTHING)
                irType.isInlinedNative() -> TypeWithKind(erasedType, TypeKind.VALUE_TYPE)
                irType.classifierOrFail is IrTypeParameterSymbol -> TypeWithKind(erasedType, TypeKind.GENERIC)
                else -> TypeWithKind(erasedType, TypeKind.REFERENCE)
            }
        }
    }
}

private fun IrFunction.typeWithKindAt(index: ParameterIndex) = when (index) {
    ParameterIndex.RETURN_INDEX -> when {
        isSuspend -> TypeWithKind(null, TypeKind.REFERENCE)
        returnType.isVoidAsReturnType() -> TypeWithKind(returnType, TypeKind.UNIT)
        else -> TypeWithKind.fromType(returnType)
    }
    ParameterIndex.DISPATCH_RECEIVER_INDEX -> TypeWithKind.fromType(dispatchReceiverParameter?.type)
    ParameterIndex.EXTENSION_RECEIVER_INDEX -> TypeWithKind.fromType(extensionReceiverParameter?.type)
    else -> TypeWithKind.fromType(this.valueParameters[index.unmap()].type)
}

private fun IrFunction.needBridgeToAt(target: IrFunction, index: ParameterIndex)
        = bridgeDirectionToAt(target, index).kind != BridgeDirectionKind.NONE

@JvmInline
private value class ParameterIndex(val index: Int) {
    companion object {
        val RETURN_INDEX = ParameterIndex(0)
        val DISPATCH_RECEIVER_INDEX = ParameterIndex(1)
        val EXTENSION_RECEIVER_INDEX = ParameterIndex(2)

        fun map(index: Int) = ParameterIndex(index + 3)

        fun allParametersCount(irFunction: IrFunction) = irFunction.valueParameters.size + 3

        inline fun forEachIndex(irFunction: IrFunction, block: (ParameterIndex) -> Unit) =
                (0 until allParametersCount(irFunction)).forEach { block(ParameterIndex(it)) }
    }

    fun unmap() = index - 3
}

internal fun IrFunction.needBridgeTo(target: IrFunction): Boolean {
    ParameterIndex.forEachIndex(this) {
        if (needBridgeToAt(target, it)) return true
    }
    return false
}

internal enum class BridgeDirectionKind {
    NONE,
    BOX,
    UNBOX,
    AS_IS,
    CAST
}

internal data class BridgeDirection(val erasedType: IrType?, val kind: BridgeDirectionKind) {
    companion object {
        val NONE = BridgeDirection(null, BridgeDirectionKind.NONE)
        val AS_IS = BridgeDirection(null, BridgeDirectionKind.AS_IS)
        val BOX = BridgeDirection(null, BridgeDirectionKind.BOX)
    }
}

/*
 *   left to right : overridden
 *   up to down    : function
 *
 *  +-----+-----+-----+-----+-----+-----+
 *  |  \  |  ⊥  |  () | VAL | REF | <T> |
 *  +-----+-----+-----+-----+-----+-----+
 *  |  ⊥  |  N  |  E  |  E  |  E  |  E  |
 *  +-----+-----+-----+-----+-----+-----+
 *  |  () |  E  |  N  |  A  |  A  |  A  |
 *  +-----+-----+-----+-----+-----+-----+
 *  | VAL |  E  |  B  |  N  |  B  |  B  |
 *  +-----+-----+-----+-----+-----+-----+
 *  | REF |  E  |  N  |  U  |  N  | C^N |
 *  +-----+-----+-----+-----+-----+-----+
 *  | <T> |  E  |  N  |  U  | C^N |  N  |
 *  +-----+-----+-----+-----+-----+-----+
 */

private typealias BridgeDirectionBuilder = (ParameterIndex, IrType?, IrType?) -> BridgeDirection

private val None: BridgeDirectionBuilder = { _, _, _ -> BridgeDirection.NONE }
private val AsIs: BridgeDirectionBuilder = { _, _, _ -> BridgeDirection.AS_IS }
private val Box: BridgeDirectionBuilder = { _, _, _ -> BridgeDirection.BOX }
private val Unbox: BridgeDirectionBuilder = { _, _, to -> BridgeDirection(to, BridgeDirectionKind.UNBOX) }
private val Cast: BridgeDirectionBuilder = { index, from, to ->
    if (index == ParameterIndex.RETURN_INDEX) {
        // from as to
        if (from == null || to == null || from.classOrFail.owner.isNothing() || from.isSubtypeOfClass(to.classOrFail))
            BridgeDirection.NONE
        else
            BridgeDirection(to, BridgeDirectionKind.CAST)
    } else {
        // to as from
        if (from == null || to == null || from.classOrFail.let { it.owner.isNothing() || to.isSubtypeOfClass(it) })
            BridgeDirection.NONE
        else
            BridgeDirection(to, BridgeDirectionKind.CAST)
    }
}

private val bridgeDirectionBuilders = arrayOf(
        arrayOf(None, null, null, null, null),
        arrayOf(null, None, AsIs, AsIs, AsIs),
        arrayOf(null, Box, None, Box, Box),
        arrayOf(null, None, Unbox, None, Cast),
        arrayOf(null, None, Unbox, Cast, None),
)

private fun IrFunction.bridgeDirectionToAt(overriddenFunction: IrFunction, index: ParameterIndex): BridgeDirection {
    val (fromErasedType, fromKind) = typeWithKindAt(index)
    val (toErasedType, toKind) = overriddenFunction.typeWithKindAt(index)
    val bridgeDirectionsBuilder = bridgeDirectionBuilders[fromKind.ordinal][toKind.ordinal]
            ?: error("Invalid combination of (fromKind, toKind): ($fromKind, $toKind)\n" +
                    "from = ${render()}\nto = ${overriddenFunction.render()}\n" +
                    "from class = ${this.parentClassOrNull?.render()}\nto class = ${overriddenFunction.parentClassOrNull?.render()}")
    return bridgeDirectionsBuilder(index, fromErasedType, toErasedType)/*.also {
        if (index == ParameterIndex.RETURN_INDEX && it.kind == BridgeDirectionKind.CAST) {
            val t = IllegalStateException()
            t.printStackTrace()
            println("ZZZ from = ${render()}\n    to = ${overriddenFunction.render()}")
            println("    from class = ${this.parentClassOrNull?.render()}\n" +
                    "    to class = ${overriddenFunction.parentClassOrNull?.render()}")
            println("    fromErasedType = ${fromErasedType?.render()}")
            println("    toErasedType = ${toErasedType?.render()}")
        }
    }*/
}

//private fun TypeKind.withoutGeneric() = if (this == TypeKind.GENERIC) TypeKind.REFERENCE else this
//
//private fun IrFunction.bridgeDirectionToAt(overriddenFunction: IrFunction, index: ParameterIndex): BridgeDirection {
//    val kind = typeWithKindAt(index).kind.withoutGeneric()
//    val (erasedUpperBound, otherKind) = overriddenFunction.typeWithKindAt(index)
//    return if (otherKind.withoutGeneric() == kind)
//        BridgeDirection.NONE
//    else when (kind) {
//        TypeKind.UNIT, TypeKind.REFERENCE -> BridgeDirection(erasedUpperBound, BridgeDirectionKind.UNBOX)
//        TypeKind.VALUE_TYPE -> BridgeDirection(
//                null,//erasedUpperBound.takeIf { otherKind == TypeKind.UNIT } /* Otherwise erase to [Any?] */,
//                BridgeDirectionKind.BOX)
//        TypeKind.NOTHING -> error("TypeKind.NOTHING should be on both sides")
//        TypeKind.GENERIC -> error("unreachable")
//    }
//}

internal class BridgeDirections(private val array: Array<BridgeDirection>) {
    constructor(irFunction: IrSimpleFunction, overriddenFunction: IrSimpleFunction)
            : this(Array<BridgeDirection>(ParameterIndex.allParametersCount(irFunction)) {
        irFunction.bridgeDirectionToAt(overriddenFunction, ParameterIndex(it))
    })

    fun allNotNeeded(): Boolean = array.all { it.kind == BridgeDirectionKind.NONE }

    private fun getDirectionAt(index: ParameterIndex) = array[index.index]

    val returnDirection get() = getDirectionAt(ParameterIndex.RETURN_INDEX)
    val dispatchReceiverDirection get() = getDirectionAt(ParameterIndex.DISPATCH_RECEIVER_INDEX)
    val extensionReceiverDirection get() = getDirectionAt(ParameterIndex.EXTENSION_RECEIVER_INDEX)
    fun parameterDirectionAt(index: Int) = getDirectionAt(ParameterIndex.map(index))

    override fun toString(): String {
        val result = StringBuilder()
        array.forEach {
            result.append(when (it.kind) {
                BridgeDirectionKind.BOX -> 'B'
                BridgeDirectionKind.UNBOX -> 'U'
                BridgeDirectionKind.AS_IS -> 'A'
                BridgeDirectionKind.CAST -> 'C' // TODO: Actually, there might be different casts here.
                BridgeDirectionKind.NONE -> 'N'
            })
        }
        return result.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BridgeDirections) return false

        return array.size == other.array.size
                && array.indices.all { array[it] == other.array[it] }
    }

    override fun hashCode(): Int {
        var result = 0
        array.forEach { result = result * 31 + it.hashCode() }
        return result
    }

    companion object {
        fun none(irFunction: IrSimpleFunction) = BridgeDirections(irFunction, irFunction)
    }
}

val IrSimpleFunction.allOverriddenFunctions: Set<IrSimpleFunction>
    get() {
        val result = mutableSetOf<IrSimpleFunction>()

        fun traverse(function: IrSimpleFunction) {
            if (function in result) return
            result += function
            function.overriddenSymbols.forEach { traverse(it.owner) }
        }

        traverse(this)

        return result
    }

internal fun IrSimpleFunction.bridgeDirectionsTo(overriddenFunction: IrSimpleFunction): BridgeDirections {
    val ourDirections = BridgeDirections(this, overriddenFunction)

    val target = this.target
    if (!this.isReal && modality != Modality.ABSTRACT
            && target.overrides(overriddenFunction)
            && ourDirections == target.bridgeDirectionsTo(overriddenFunction)) {
        // Bridge is inherited from superclass.
        return BridgeDirections.none(this)
    }

    return ourDirections
}

fun IrFunctionSymbol.isComparisonFunction(map: Map<IrClassifierSymbol, IrSimpleFunctionSymbol>): Boolean =
        this in map.values

internal fun IrClass.isFrozen(context: Context): Boolean {
    val isLegacyMM = context.memoryModel != MemoryModel.EXPERIMENTAL
    return when {
        !context.config.freezing.freezeImplicit -> false
        annotations.hasAnnotation(KonanFqNames.frozen) -> true
        annotations.hasAnnotation(KonanFqNames.frozenLegacyMM) && isLegacyMM -> true
        // RTTI is used for non-reference type box:
        !this.defaultType.binaryTypeIsReference() -> true
        else -> false
    }
}

fun IrFunction.externalSymbolOrThrow(): String? {
    annotations.findAnnotation(RuntimeNames.symbolNameAnnotation)?.let { return it.getAnnotationStringValue() }

    annotations.findAnnotation(KonanFqNames.gcUnsafeCall)?.let { return it.getAnnotationStringValue("callee") }

    if (annotations.hasAnnotation(KonanFqNames.objCMethod)) return null

    if (annotations.hasAnnotation(KonanFqNames.typedIntrinsic)) return null

    if (annotations.hasAnnotation(RuntimeNames.cCall)) return null

    throw Error("external function ${this.longName} must have @TypedIntrinsic, @SymbolName, @GCUnsafeCall or @ObjCMethod annotation")
}

private val IrFunction.longName: String
    get() = "${(parent as? IrClass)?.name?.asString() ?: "<root>"}.${(this as? IrSimpleFunction)?.name ?: "<init>"}"

val IrFunction.isBuiltInOperator get() = origin == IrBuiltIns.BUILTIN_OPERATOR
