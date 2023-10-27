/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir.konan.compiler.based

import org.jetbrains.kotlin.analysis.low.level.api.fir.compiler.based.AbstractCompilerBasedTestForFir
import org.jetbrains.kotlin.analysis.low.level.api.fir.diagnostic.compiler.based.ReversedFirIdenticalChecker
import org.jetbrains.kotlin.analysis.low.level.api.fir.diagnostic.compiler.based.facades.LLFirAnalyzerFacadeFactoryWithoutPreresolve
import org.jetbrains.kotlin.konan.test.diagnostics.baseFirNativeDiagnosticTestConfiguration
import org.jetbrains.kotlin.konan.test.diagnostics.baseNativeDiagnosticTestConfiguration
import org.jetbrains.kotlin.platform.konan.NativePlatforms
import org.jetbrains.kotlin.test.bind
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.runners.configurationForClassicAndFirTestsAlongside

abstract class AbstractCompilerBasedNativeTestForFir : AbstractCompilerBasedTestForFir() {
    override fun TestConfigurationBuilder.configureTest() {
        globalDefaults {
            targetPlatform = NativePlatforms.unspecifiedNativePlatform
        }
        baseNativeDiagnosticTestConfiguration(::LowLevelFirFrontendFacade.bind(LLFirAnalyzerFacadeFactoryWithoutPreresolve))
        baseFirNativeDiagnosticTestConfiguration()
        configurationForClassicAndFirTestsAlongside(::ReversedFirIdenticalChecker)
    }
}
