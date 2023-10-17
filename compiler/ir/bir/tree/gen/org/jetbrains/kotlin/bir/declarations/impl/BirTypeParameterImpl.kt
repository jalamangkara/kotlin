/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/ir.tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.declarations.impl

import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.SourceSpan
import org.jetbrains.kotlin.bir.declarations.BirTypeParameter
import org.jetbrains.kotlin.bir.expressions.BirConstructorCall
import org.jetbrains.kotlin.bir.types.BirType
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.util.IdSignature
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.Variance

class BirTypeParameterImpl @ObsoleteDescriptorBasedAPI constructor(
    sourceSpan: SourceSpan,
    @property:ObsoleteDescriptorBasedAPI
    override val descriptor: TypeParameterDescriptor,
    signature: IdSignature?,
    override var annotations: List<BirConstructorCall>,
    origin: IrDeclarationOrigin,
    name: Name,
    variance: Variance,
    index: Int,
    isReified: Boolean,
    override var superTypes: List<BirType>,
) : BirTypeParameter() {
    private var _sourceSpan: SourceSpan = sourceSpan

    override var sourceSpan: SourceSpan
        get() = _sourceSpan
        set(value) {
            if (_sourceSpan != value) {
                _sourceSpan = value
                invalidate()
            }
        }

    private var _signature: IdSignature? = signature

    override var signature: IdSignature?
        get() = _signature
        set(value) {
            if (_signature != value) {
                _signature = value
                invalidate()
            }
        }

    private var _origin: IrDeclarationOrigin = origin

    override var origin: IrDeclarationOrigin
        get() = _origin
        set(value) {
            if (_origin != value) {
                _origin = value
                invalidate()
            }
        }

    private var _name: Name = name

    override var name: Name
        get() = _name
        set(value) {
            if (_name != value) {
                _name = value
                invalidate()
            }
        }

    private var _variance: Variance = variance

    override var variance: Variance
        get() = _variance
        set(value) {
            if (_variance != value) {
                _variance = value
                invalidate()
            }
        }

    private var _index: Int = index

    override var index: Int
        get() = _index
        set(value) {
            if (_index != value) {
                _index = value
                invalidate()
            }
        }

    private var _isReified: Boolean = isReified

    override var isReified: Boolean
        get() = _isReified
        set(value) {
            if (_isReified != value) {
                _isReified = value
                invalidate()
            }
        }

    override fun replaceChildProperty(old: BirElement, new: BirElement?) {
        when {
            else -> throwChildForReplacementNotFound(old)
        }
    }
}
