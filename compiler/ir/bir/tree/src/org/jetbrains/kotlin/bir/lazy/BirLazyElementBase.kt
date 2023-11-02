/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.lazy

import org.jetbrains.kotlin.bir.*
import org.jetbrains.kotlin.bir.declarations.*
import org.jetbrains.kotlin.bir.expressions.BirConstructorCall
import org.jetbrains.kotlin.bir.types.BirSimpleType
import org.jetbrains.kotlin.bir.types.BirType
import org.jetbrains.kotlin.bir.util.Ir2BirConverter
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.util.IdSignature
import org.jetbrains.kotlin.name.Name

abstract class BirLazyElementBase(
    internal val converter: Ir2BirConverter,
) : BirElementBase(), BirDeclaration {
    protected abstract val originalElement: IrDeclaration

    final override val parent: BirElementBase?
        get() = TODO()

    final override fun setParentWithInvalidation(new: BirElementParent?) {
        _parent = new
    }

    internal fun initChild(new: BirLazyElementBase?) {
        if (new != null) {
            new._parent = this
            root?.elementAttached(new)
        }
    }

    internal open fun getOriginalChildList(list: BirLazyChildElementList<*>): List<IrElement> {
        throw NotImplementedError("Child list with id ${list.id} is not handled in getOriginalChildList")
    }


    override val sourceSpan: SourceSpan
        get() = SourceSpan(originalElement.startOffset, originalElement.endOffset)

    override var signature: IdSignature?
        get() = originalElement.symbol.signature
        set(value) = mutationNotSupported()


    override var origin: IrDeclarationOrigin
        get() = originalElement.origin
        set(value) = mutationNotSupported()


    final override fun replaceWith(new: BirElement?) = mutationNotSupported()

    companion object {
        internal fun mutationNotSupported(): Nothing =
            error("Mutation of lazy BIR elements is not possible")
    }
}

class BirLazyClass(override val originalElement: IrClass, converter: Ir2BirConverter) : BirLazyElementBase(converter), BirClass {
    private fun convert() {
        val birElement = converter.remapElement<BirClass>(originalElement)
        kind = birElement.kind
    }

    override val owner: BirClass
        get() = this

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    override val descriptor: ClassDescriptor
        get() = originalElement.descriptor

    override var kind: ClassKind
        get() = originalElement.kind
        set(value) = mutationNotSupported()
    override var modality: Modality
        get() = originalElement.modality
        set(value) = mutationNotSupported()
    override var isCompanion: Boolean
        get() = originalElement.isCompanion
        set(value) = mutationNotSupported()
    override var isInner: Boolean
        get() = originalElement.isInner
        set(value) = mutationNotSupported()
    override var isData: Boolean
        get() = originalElement.isData
        set(value) = mutationNotSupported()
    override var isValue: Boolean
        get() = originalElement.isValue
        set(value) = mutationNotSupported()
    override var isExpect: Boolean
        get() = originalElement.isExpect
        set(value) = mutationNotSupported()
    override var isFun: Boolean
        get() = originalElement.isFun
        set(value) = mutationNotSupported()
    override var hasEnumEntries: Boolean
        get() = originalElement.hasEnumEntries
        set(value) = mutationNotSupported()
    override val source: SourceElement
        get() = originalElement.source
    override var name: Name
        get() = originalElement.name
        set(value) = mutationNotSupported()
    override var visibility: DescriptorVisibility
        get() = originalElement.visibility
        set(value) = mutationNotSupported()
    override var isExternal: Boolean
        get() = originalElement.isExternal
        set(value) = mutationNotSupported()

    override var valueClassRepresentation: ValueClassRepresentation<BirSimpleType>?
        //get() = originalElement.valueClassRepresentation
        get() = TODO()
        set(value) = mutationNotSupported()
    override var superTypes: List<BirType>
        //get() = originalElement.superTypes
        get() = TODO()
        set(value) = mutationNotSupported()
    override var typeParameters: BirChildElementList<BirTypeParameter>
        //get() = originalElement.typeParameters
        get() = TODO()
        set(value) = mutationNotSupported()
    override var attributeOwnerId: BirAttributeContainer
        //get() = originalElement.attributeOwnerId
        get() = TODO()
        set(value) = mutationNotSupported()

    override var thisReceiver: BirValueParameter?
        //get() = originalElement.thisReceiver
        get() = TODO()
        set(value) = mutationNotSupported()
    override var annotations: BirChildElementList<BirConstructorCall>
        //get() = originalElement.annotations
        get() = TODO()
        set(value) = mutationNotSupported()
    override val declarations = BirLazyChildElementList<BirDeclaration>(this, 0, false)

    override fun getOriginalChildList(list: BirLazyChildElementList<*>): List<IrElement> {
        return when (list) {
            declarations -> originalElement.declarations
            else -> super.getOriginalChildList(list)
        }
    }
}