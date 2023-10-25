/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.declarations

import org.jetbrains.kotlin.fir.symbols.impl.FirFieldSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol

private object DelegateFieldsMapKey : FirDeclarationDataKey()

/*
 * If a class implements some interfaces using delegation, then this attribute contains mapping
 *   from index of supertype to symbol of delegated field
 */
var FirClass.delegateFieldsMap: Map<Int, FirFieldSymbol>? by FirDeclarationDataRegistry.data(DelegateFieldsMapKey)

private object CorrespondingPropertyForDelegateField : FirDeclarationDataKey()

/*
 * If implementation of some interface is delegated to class property (from the primary constructor) then FIR still
 *   creates a synthetic delegate field. And for such cases, this attribute shows the relation between field and property
 */
var FirField.correspondingPropertyForDelegate: FirPropertySymbol? by FirDeclarationDataRegistry.data(CorrespondingPropertyForDelegateField)
