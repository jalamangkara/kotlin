/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.runners

import org.jetbrains.kotlin.config.JvmTarget
import org.jetbrains.kotlin.test.FirParser
import org.jetbrains.kotlin.test.TargetBackend
import org.jetbrains.kotlin.test.TestJdkKind
import org.jetbrains.kotlin.test.backend.handlers.AbiConsistencyHandler
import org.jetbrains.kotlin.test.backend.ir.AbiCheckerSuppressor
import org.jetbrains.kotlin.test.backend.ir.BothFrontendsIrActualizerAndPluginsFacade
import org.jetbrains.kotlin.test.backend.ir.BothFrontendsJvmIrBackendFacade
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.builders.configureTwoJvmArtifactHandlerStep
import org.jetbrains.kotlin.test.builders.twoJvmArtifactsHandlersStep
import org.jetbrains.kotlin.test.directives.*
import org.jetbrains.kotlin.test.frontend.Both2IrConverter
import org.jetbrains.kotlin.test.frontend.BothFrontendsFacade
import org.jetbrains.kotlin.test.model.FrontendKinds
import org.jetbrains.kotlin.test.runners.codegen.commonServicesConfigurationForCodegenAndDebugTest
import org.jetbrains.kotlin.test.services.configuration.JavaForeignAnnotationType
import org.jetbrains.kotlin.test.services.configuration.JvmForeignAnnotationsConfigurator

open class AbstractAbiConsistencyTest :
    AbstractKotlinCompilerWithTargetBackendTest(TargetBackend.JVM_IR),
    RunnerWithTargetBackendForTestGeneratorMarker {

    override fun TestConfigurationBuilder.configuration() {

        commonServicesConfigurationForCodegenAndDebugTest(FrontendKinds.Both)

        defaultDirectives {
            FirDiagnosticsDirectives.FIR_PARSER with FirParser.Psi
        }

        forTestsMatching("compiler/testData/codegen/bytecodeText/*") {
            defaultDirectives {
                +ConfigurationDirectives.WITH_STDLIB
                +JvmEnvironmentConfigurationDirectives.WITH_REFLECT
            }
        }

        forTestsMatching("compiler/testData/codegen/boxModernJdk/*") {
            defaultDirectives {
                +ConfigurationDirectives.WITH_STDLIB
                +CodegenTestDirectives.USE_JAVAC_BASED_ON_JVM_TARGET
                +CodegenTestDirectives.IGNORE_DEXING
            }
        }

        forTestsMatching("compiler/testData/codegen/boxModernJdk/testsWithJava11/*") {
            defaultDirectives {
                JvmEnvironmentConfigurationDirectives.JDK_KIND with TestJdkKind.FULL_JDK_11
                JvmEnvironmentConfigurationDirectives.JVM_TARGET with JvmTarget.JVM_11
            }
        }

        forTestsMatching("compiler/testData/codegen/boxModernJdk/testsWithJava17/*") {
            defaultDirectives {
                JvmEnvironmentConfigurationDirectives.JDK_KIND with TestJdkKind.FULL_JDK_17
                JvmEnvironmentConfigurationDirectives.JVM_TARGET with JvmTarget.JVM_17
            }
        }

        forTestsMatching("compiler/testData/codegen/box/javaInterop/foreignAnnotationsTests/tests/*") {
            defaultDirectives {
                ForeignAnnotationsDirectives.ANNOTATIONS_PATH with JavaForeignAnnotationType.Annotations
            }
            useConfigurators(::JvmForeignAnnotationsConfigurator)
        }

        useAfterAnalysisCheckers(
            ::AbiCheckerSuppressor
        )

        facadeStep(::BothFrontendsFacade)
        facadeStep(::Both2IrConverter)
        facadeStep(::BothFrontendsIrActualizerAndPluginsFacade)
        facadeStep(::BothFrontendsJvmIrBackendFacade)

        twoJvmArtifactsHandlersStep {}

        configureTwoJvmArtifactHandlerStep {
            useHandlers(::AbiConsistencyHandler)
        }

    }
}