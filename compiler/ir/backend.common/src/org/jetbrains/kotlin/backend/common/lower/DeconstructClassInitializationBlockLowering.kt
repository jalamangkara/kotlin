/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.lower

import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.DeclarationTransformer
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.symbols.IrValueParameterSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrValueParameterSymbolImpl
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.Name

private object DECONSTRUCTED_INIT_BLOCK_ORIGIN : IrDeclarationOriginImpl("DECONSTRUCTED_INIT_BLOCK_ORIGIN")

/**
 * The lowering move initialization stage body into separated inline function to declare init block captures explicitly
 * It helps to handle initialization blocks as a regular function inside [org.jetbrains.kotlin.backend.common.lower.LocalDeclarationsLowering]
 */
class DeconstructClassInitializationBlockLowering(private val context: CommonBackendContext) : DeclarationTransformer {
    override fun transformFlat(declaration: IrDeclaration): List<IrDeclaration>? {
        if (declaration !is IrAnonymousInitializer) return null
        val blockOwner = declaration.parentAsClass
        val blockOwnerSelf = blockOwner.thisReceiver ?: error("Expect `thisReceiver` to be set")

        val initializationFunction = context.irFactory.buildFun {
            name = Name.identifier("initBlock")
            returnType = context.irBuiltIns.unitType
            visibility = DescriptorVisibilities.PRIVATE
            modality = Modality.FINAL
            isInline = true
            isExternal = false
            origin = DECONSTRUCTED_INIT_BLOCK_ORIGIN
        }.apply {
            parent = blockOwner
            dispatchReceiverParameter = blockOwnerSelf
            body = declaration.body
        }

        declaration.body = context.createIrBuilder(declaration.symbol).irBlockBody {
            +irCall(initializationFunction.symbol).apply {
                dispatchReceiver = irGet(blockOwnerSelf)
            }
        }

        return listOf(declaration, initializationFunction)
    }
}

class RemoveDeconstructedInitBlockAfterInliningLowering : DeclarationTransformer {
    override fun transformFlat(declaration: IrDeclaration): List<IrDeclaration>? {
        if (declaration.origin == DECONSTRUCTED_INIT_BLOCK_ORIGIN) {
            return emptyList()
        }

        return null
    }

}
