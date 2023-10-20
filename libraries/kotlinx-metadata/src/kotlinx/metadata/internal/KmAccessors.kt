/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlinx.metadata.internal

import kotlinx.metadata.KmClass
import kotlinx.metadata.KmLambda
import kotlinx.metadata.KmPackage

public object KmInternalAccessors {
    public fun disallowWrite(kmClass: KmClass) {
        kmClass.writeDisallowed = true
    }

    public fun isWriteable(kmClass: KmClass): Boolean = !kmClass.writeDisallowed

    public fun disallowWrite(kmPackage: KmPackage) {
        kmPackage.writeDisallowed = true
    }

    public fun isWriteable(kmPackage: KmPackage): Boolean = !kmPackage.writeDisallowed

    public fun disallowWrite(kmLambda: KmLambda) {
        kmLambda.writeDisallowed = true
    }

    public fun isWriteable(kmLambda: KmLambda): Boolean = !kmLambda.writeDisallowed
}
