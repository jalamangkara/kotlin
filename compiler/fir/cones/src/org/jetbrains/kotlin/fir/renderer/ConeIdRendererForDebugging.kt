/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.renderer

import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId

class ConeIdRendererForDebugging : ConeIdRenderer() {
    override fun renderDeclarationClassId(classId: ClassId) {
        builder.append(classId.asString())
    }

    override fun renderReferenceClassId(classId: ClassId) {
        builder.append(classId.toString())
    }

    override fun renderDeclarationCallableId(callableId: CallableId) {
        builder.append(callableId.callableName)
    }

    override fun renderReferenceCallableId(callableId: CallableId) {
        builder.append(callableId)
    }
}