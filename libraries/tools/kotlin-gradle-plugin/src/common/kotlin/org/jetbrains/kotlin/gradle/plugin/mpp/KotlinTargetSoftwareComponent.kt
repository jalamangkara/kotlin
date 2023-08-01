/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp

import org.gradle.api.component.ComponentWithCoordinates
import org.gradle.api.component.ComponentWithVariants
import org.gradle.api.internal.component.SoftwareComponentInternal
import org.jetbrains.kotlin.gradle.utils.Future

abstract class KotlinTargetSoftwareComponent : ComponentWithVariants, ComponentWithCoordinates, SoftwareComponentInternal {
    internal abstract val configured: Future<KotlinTargetSoftwareComponent>
}