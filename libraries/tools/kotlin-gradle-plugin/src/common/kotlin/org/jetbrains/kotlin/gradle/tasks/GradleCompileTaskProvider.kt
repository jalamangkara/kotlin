/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.tasks

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.Logger
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import org.jetbrains.kotlin.compilerRunner.GradleCompilerRunner.Companion.normalizeForFlagFile
import org.jetbrains.kotlin.gradle.incremental.IncrementalModuleInfoProvider
import org.jetbrains.kotlin.gradle.utils.property
import org.jetbrains.kotlin.gradle.utils.sessionsDir
import java.io.File
import javax.inject.Inject

abstract class GradleCompileTaskProvider @Inject constructor(
    objectFactory: ObjectFactory,
    projectLayout: ProjectLayout,
    gradle: Gradle,
    task: Task,
    project: Project,
    incrementalModuleInfoProvider: Provider<IncrementalModuleInfoProvider>
) {

    @get:Internal
    val path: Provider<String> = objectFactory.property(task.path)

    @get:Internal
    val logger: Provider<Logger> = objectFactory.property(task.logger)

    @get:Internal
    val buildDir: DirectoryProperty = projectLayout.buildDirectory

    @get:Internal
    val projectDir: Provider<File> = objectFactory
        .property(project.rootProject.projectDir)

    @get:Internal
    val sessionsDir: Provider<File> = objectFactory
        .property(project.sessionsDir)

    @get:Internal
    val projectName: Provider<String> = objectFactory
        .property(project.rootProject.name.normalizeForFlagFile())

    @get:Internal
    val buildModulesInfo: Provider<out IncrementalModuleInfoProvider> = objectFactory
        .property(incrementalModuleInfoProvider)

    @get:Internal
    val errorsFile: Provider<File?> = objectFactory
        .property(
            gradle.rootProject.rootDir.resolve(".gradle/kotlin/errors/").also { it.mkdirs() }
                .resolve("errors-${System.currentTimeMillis()}.log"))
}