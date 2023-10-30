/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.ir

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.DeprecatedTargetPresetApi
import org.jetbrains.kotlin.gradle.plugin.AbstractKotlinTargetConfigurator
import org.jetbrains.kotlin.gradle.plugin.KotlinOnlyTargetConfigurator
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinCompilationFactory
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJsCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinOnlyTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinOnlyTargetPreset
import org.jetbrains.kotlin.gradle.plugin.statistics.KotlinBuildStatsService
import org.jetbrains.kotlin.gradle.utils.runProjectConfigurationHealthCheckWhenEvaluated
import org.jetbrains.kotlin.statistics.metrics.StringMetrics

@DeprecatedTargetPresetApi
open class KotlinJsIrTargetPreset(
    project: Project,
) : KotlinOnlyTargetPreset<KotlinJsIrTarget, KotlinJsCompilation>(
    project
) {
    open val isMpp: Boolean
        get() = true

    override val platformType: KotlinPlatformType = KotlinPlatformType.js

    override fun instantiateTarget(name: String): KotlinJsIrTarget {
        return project.objects.newInstance(KotlinJsIrTarget::class.java, project, platformType).apply {
            this.isMpp = this@KotlinJsIrTargetPreset.isMpp
            project.runProjectConfigurationHealthCheckWhenEvaluated {

                val buildStatsService = KotlinBuildStatsService.getInstance()
                when {
                    isBrowserConfigured && isNodejsConfigured -> buildStatsService?.report(StringMetrics.JS_TARGET_MODE, "both")
                    isBrowserConfigured -> buildStatsService?.report(StringMetrics.JS_TARGET_MODE, "browser")
                    isNodejsConfigured -> buildStatsService?.report(StringMetrics.JS_TARGET_MODE, "nodejs")
                    !isBrowserConfigured && !isNodejsConfigured -> buildStatsService?.report(StringMetrics.JS_TARGET_MODE, "none")
                }
                Unit
            }
        }
    }

    override fun createKotlinTargetConfigurator(): AbstractKotlinTargetConfigurator<KotlinJsIrTarget> =
        KotlinJsIrTargetConfigurator()

    override fun getName(): String = JS_PRESET_NAME

    //TODO[Ilya Goncharov] remove public morozov
    public override fun createCompilationFactory(
        forTarget: KotlinJsIrTarget,
    ): KotlinCompilationFactory<KotlinJsCompilation> =
        KotlinJsIrCompilationFactory(forTarget)

    companion object {
        val JS_PRESET_NAME = "js"
    }
}

@DeprecatedTargetPresetApi
class KotlinJsIrSingleTargetPreset(
    project: Project,
) : KotlinJsIrTargetPreset(
    project
) {
    override val isMpp: Boolean
        get() = false

    // In a Kotlin/JS single-platform project, we don't need any disambiguation suffixes or prefixes in the names:
    override fun provideTargetDisambiguationClassifier(target: KotlinOnlyTarget<KotlinJsCompilation>): String? {
        return null
    }

    override fun createKotlinTargetConfigurator(): KotlinOnlyTargetConfigurator<KotlinJsCompilation, KotlinJsIrTarget> =
        KotlinJsIrTargetConfigurator()
}
