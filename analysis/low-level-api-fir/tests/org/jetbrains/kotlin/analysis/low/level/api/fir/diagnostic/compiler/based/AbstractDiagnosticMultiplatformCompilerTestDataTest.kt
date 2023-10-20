/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir.diagnostic.compiler.based

abstract class AbstractDiagnosticMultiplatformCompilerTestDataTest : AbstractDiagnosticCompilerTestDataTest() {
    // For multiplatform tests it's expected that LL and FIR diverge,
    // because IR actualizer doesn't run in IDE mode tests.
    override val checkLLFirDivergenceComment: Boolean = false
}