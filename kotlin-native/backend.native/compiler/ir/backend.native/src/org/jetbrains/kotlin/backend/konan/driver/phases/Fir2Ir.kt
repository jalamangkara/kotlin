/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.konan.driver.phases

import org.jetbrains.kotlin.backend.konan.driver.PhaseEngine
import org.jetbrains.kotlin.backend.konan.fir2Ir
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.backend.Fir2IrResult
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.fir.resolve.ScopeSession

data class Fir2IrOutput(
        val session: FirSession,
        val scopeSession: ScopeSession,
        val firFiles: List<FirFile>,
        val fir2irResult: Fir2IrResult,
)

internal fun <T : FirFrontendContext> PhaseEngine<T>.runFir2Ir(input: FirOutput.Full): Fir2IrOutput {
    return this.runPhase(Fir2IrPhase, input)!!
}

internal val Fir2IrPhase = createSimpleNamedCompilerPhase(
        "Fir2Ir", "Compiler Fir2Ir Frontend phase",
        outputIfNotEnabled = { _, _, _, _ -> null }
) { context: FirFrontendContext, input: FirOutput.Full ->
    context.fir2Ir(input, context)
}
