/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.backend.ir

import org.jetbrains.kotlin.test.model.*
import org.jetbrains.kotlin.test.runWithEnablingFirUseOption
import org.jetbrains.kotlin.test.services.TestServices

class BothFrontendsJvmIrBackendFacade(testServices: TestServices) :
    BackendFacade<IrBackendInputsFromBothFrontends, BinaryArtifacts.TwoJvm>(
        testServices,
        BackendKinds.TwoIrBackends,
        ArtifactKinds.TwoJvm
    ) {

    private val backendForClassicFrontend = JvmIrBackendFacade(testServices)
    private val backendForFir = JvmIrBackendFacade(testServices)

    override fun transform(module: TestModule, inputArtifact: IrBackendInputsFromBothFrontends): BinaryArtifacts.TwoJvm? {
        val fromClassicFrontend = backendForClassicFrontend.transform(module, inputArtifact.fromClassicFrontend) ?: return null
        val fromFir =
            runWithEnablingFirUseOption(testServices, module) { backendForFir.transform(module, inputArtifact.fromFir) } ?: return null
        return BinaryArtifacts.TwoJvm(fromClassicFrontend, fromFir)
    }

    override fun shouldRunAnalysis(module: TestModule): Boolean {
        return module.backendKind == BackendKinds.IrBackend && module.binaryKind == ArtifactKinds.TwoJvm
    }
}