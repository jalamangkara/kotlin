/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.backend.ir

import org.jetbrains.kotlin.test.model.AbstractTestFacade
import org.jetbrains.kotlin.test.model.BackendKinds
import org.jetbrains.kotlin.test.model.TestArtifactKind
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.runWithEnablingFirUseOption
import org.jetbrains.kotlin.test.services.TestServices

class BothFrontendsIrActualizerAndPluginsFacade(val testServices: TestServices) :
    AbstractTestFacade<IrBackendInputsFromBothFrontends, IrBackendInputsFromBothFrontends>() {
    override val inputKind: TestArtifactKind<IrBackendInputsFromBothFrontends>
        get() = BackendKinds.TwoIrBackends
    override val outputKind: TestArtifactKind<IrBackendInputsFromBothFrontends>
        get() = BackendKinds.TwoIrBackends

    override fun shouldRunAnalysis(module: TestModule): Boolean {
        return true
    }

    private val irActualizerAndPluginsFacade = IrActualizerAndPluginsFacade(testServices)

    override fun transform(module: TestModule, inputArtifact: IrBackendInputsFromBothFrontends): IrBackendInputsFromBothFrontends? {
        val fromFir =
            runWithEnablingFirUseOption(testServices, module) { irActualizerAndPluginsFacade.transform(module, inputArtifact.fromFir) }
        return IrBackendInputsFromBothFrontends(inputArtifact.fromClassicFrontend, fromFir)
    }

}