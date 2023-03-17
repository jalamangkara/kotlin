/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.compilerRunner

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.logging.Logging
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.process.ExecOperations
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkQueue
import org.gradle.workers.WorkerExecutor
import org.jetbrains.kotlin.api.*
import org.jetbrains.kotlin.api.FacadeLogLevel.*
import org.jetbrains.kotlin.build.report.metrics.BuildMetricsReporter
import org.jetbrains.kotlin.build.report.metrics.BuildPerformanceMetric
import org.jetbrains.kotlin.build.report.metrics.BuildTime
import org.jetbrains.kotlin.build.report.metrics.measure
import org.jetbrains.kotlin.gradle.logging.GradleKotlinLogger
import org.jetbrains.kotlin.gradle.logging.SL4JKotlinLogger
import org.jetbrains.kotlin.gradle.plugin.internal.state.TaskLoggers
import org.jetbrains.kotlin.gradle.tasks.*
import org.jetbrains.kotlin.gradle.tasks.CompilationErrorException
import org.jetbrains.kotlin.gradle.tasks.FailedCompilationException
import org.jetbrains.kotlin.gradle.tasks.OOMErrorException
import org.jetbrains.kotlin.gradle.tasks.TaskOutputsBackup
import org.jetbrains.kotlin.incremental.*
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URLClassLoader
import java.util.*
import javax.inject.Inject

internal class GradleIncrementalCompilationFacadeRunner(
    taskProvider: GradleCompileTaskProvider,
    jdkToolsJar: File?,
    compilerExecutionSettings: CompilerExecutionSettings,
    buildMetrics: BuildMetricsReporter,
    private val workerExecutor: WorkerExecutor
) : GradleCompilerRunner(taskProvider, jdkToolsJar, compilerExecutionSettings, buildMetrics) {
    override fun runCompilerAsync(
        workArgs: GradleKotlinCompilerWorkArguments,
        taskOutputsBackup: TaskOutputsBackup?
    ): WorkQueue {

        buildMetrics.addTimeMetric(BuildPerformanceMetric.CALL_WORKER)
        val workQueue = workerExecutor.noIsolation()
        workQueue.submit(IncrementalFacadeRunnerAction::class.java) { params ->
            params.compilerWorkArguments.set(workArgs)
            if (taskOutputsBackup != null) {
                params.taskOutputsToRestore.set(taskOutputsBackup.outputsToRestore)
                params.buildDir.set(taskOutputsBackup.buildDirectory)
                params.snapshotsDir.set(taskOutputsBackup.snapshotsDir)
                params.metricsReporter.set(buildMetrics)
            }
        }
        return workQueue
    }

    internal abstract class IncrementalFacadeRunnerAction @Inject constructor(
        private val fileSystemOperations: FileSystemOperations,
        private val execOperations: ExecOperations,
    ) : WorkAction<IncrementalFacadeRunnerParameters> {
        private val logger = Logging.getLogger("kotlin-compile-worker")

        private val workArguments
            get() = parameters.compilerWorkArguments.get()

        private fun prepareLaunchOptions(): LaunchOptions = when (workArguments.compilerExecutionSettings.strategy) {
            KotlinCompilerExecutionStrategy.DAEMON -> LaunchOptions.Daemon(
                "",
                workArguments.compilerFullClasspath,
                workArguments.compilerExecutionSettings.daemonJvmArgs ?: listOf(),
                GradleKotlinCompilerLauncher(execOperations),
            )
            KotlinCompilerExecutionStrategy.IN_PROCESS -> LaunchOptions.InProcess
            else -> error("Unsupported compiler execution strategy: ${workArguments.compilerExecutionSettings.strategy}")
        }

        private fun prepareKotlinCompilerOptions(): CompilationOptions {
            val icEnv = workArguments.incrementalCompilationEnvironment
            val kotlinScriptExtensions = workArguments.kotlinScriptExtensions
            return if (icEnv != null) {
                val knownChangedFiles = icEnv.changedFiles as? ChangedFiles.Known
                val incrementalModuleInfo = workArguments.incrementalModuleInfo
                val outputFiles = workArguments.outputFiles
                CompilationOptions.Incremental(
                    areFileChangesKnown = knownChangedFiles != null,
                    modifiedFiles = knownChangedFiles?.modified,
                    deletedFiles = knownChangedFiles?.removed,
                    classpathChanges = icEnv.classpathChanges,
                    workingDir = icEnv.workingDir,
                    reportCategories = emptyArray(),
                    reportSeverity = 0,
                    requestedCompilationResults = emptyArray(),
                    compilerMode = org.jetbrains.kotlin.api.CompilerMode.INCREMENTAL_COMPILER,
                    targetPlatform = TargetPlatform.JVM,
                    usePreciseJavaTracking = icEnv.usePreciseJavaTracking,
                    outputFiles = outputFiles,
                    multiModuleICSettings = MultiModuleICSettings(
                        icEnv.multiModuleICSettings.buildHistoryFile,
                        icEnv.multiModuleICSettings.useModuleDetection,
                    ),
                    modulesInfo = incrementalModuleInfo!!,
                    kotlinScriptExtensions = kotlinScriptExtensions,
                    withAbiSnapshot = icEnv.withAbiSnapshot,
                    preciseCompilationResultsBackup = icEnv.preciseCompilationResultsBackup,
                )
            } else {
                CompilationOptions.NonIncremental(
                    compilerMode = org.jetbrains.kotlin.api.CompilerMode.NON_INCREMENTAL_COMPILER,
                    targetPlatform = TargetPlatform.JVM,
                    reportCategories = emptyArray(),
                    reportSeverity = 0,
                    requestedCompilationResults = emptyArray(),
                    kotlinScriptExtensions = kotlinScriptExtensions
                )
            }
        }

        private val taskPath
            get() = workArguments.taskPath
        private val log: KotlinLogger =
            TaskLoggers.get(taskPath)?.let { GradleKotlinLogger(it).apply { debug("Using '$taskPath' logger") } }
                ?: run {
                    val logger = LoggerFactory.getLogger("GradleKotlinCompilerWork")
                    val kotlinLogger = if (logger is org.gradle.api.logging.Logger) {
                        GradleKotlinLogger(logger)
                    } else SL4JKotlinLogger(logger)

                    kotlinLogger.apply {
                        debug("Could not get logger for '$taskPath'. Falling back to sl4j logger")
                    }
                }

        override fun execute() {
            val taskOutputsBackup = if (parameters.snapshotsDir.isPresent) {
                TaskOutputsBackup(
                    fileSystemOperations,
                    parameters.buildDir,
                    parameters.snapshotsDir,
                    parameters.taskOutputsToRestore.get(),
                    logger
                )
            } else {
                null
            }

            try {
                val classpath = workArguments.compilerFacadeClasspath
                logger.warn("IC facade classpath: $classpath")
                // TODO: cache classloader
                val parentClassloader = LimitedScopeClassLoaderDelegator(
                    IncrementalCompilerFacade::class.java.classLoader,
                    ClassLoader.getSystemClassLoader(),
                    listOf("org.jetbrains.kotlin.api") // need to share API classes, so we could use them here without reflection
                )
                val classloader = URLClassLoader(classpath.toList().map { it.toURI().toURL() }.toTypedArray(), parentClassloader)
                val facade = ServiceLoader.load(IncrementalCompilerFacade::class.java, classloader).singleOrNull()
                    ?: error("Compiler classpath should contain one and only one implementation of ${IncrementalCompilerFacade::class.java.name}")
                val messageLogger = GradleFacadeMessageLogger(log, workArguments.allWarningsAsErrors)
                facade.compile(
                    prepareLaunchOptions(),
                    workArguments.compilerArgs.toList(),
                    prepareKotlinCompilerOptions(),
                    Callbacks(messageLogger)
                )
            } catch (e: FailedCompilationException) {
                // Restore outputs only in cases where we expect that the user will make some changes to their project:
                //   - For a compilation error, the user will need to fix their source code
                //   - For an OOM error, the user will need to increase their memory settings
                // In the other cases where there is nothing the user can fix in their project, we should not restore the outputs.
                // Otherwise, the next build(s) will likely fail in exactly the same way as this build because their inputs and outputs are
                // the same.
                if (taskOutputsBackup != null && (e is CompilationErrorException || e is OOMErrorException)) {
                    parameters.metricsReporter.get().measure(BuildTime.RESTORE_OUTPUT_FROM_BACKUP) {
                        logger.info("Restoring task outputs to pre-compilation state")
                        taskOutputsBackup.restoreOutputs()
                    }
                }

                throw e
            } finally {
                taskOutputsBackup?.deleteSnapshot()
            }
        }
    }

    internal interface IncrementalFacadeRunnerParameters : WorkParameters {
        val compilerWorkArguments: Property<GradleKotlinCompilerWorkArguments>
        val taskOutputsToRestore: ListProperty<File>
        val snapshotsDir: DirectoryProperty
        val buildDir: DirectoryProperty
        val metricsReporter: Property<BuildMetricsReporter>
    }
}

private class LimitedScopeClassLoaderDelegator(
    private val parent: ClassLoader,
    fallback: ClassLoader,
    private val allowedPackage: List<String>,
) : ClassLoader(fallback) {
    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        return if (allowedPackage.any { name.startsWith(it) }) {
            parent.loadClass(name)
        } else {
            super.loadClass(name, resolve)
        }
    }
}

private class GradleFacadeMessageLogger(private val log: KotlinLogger, private val warningsAsErrors: Boolean) : MessageLogger {
    override fun report(level: FacadeLogLevel, message: String) {
        when (level) {
            INFO -> log.info("i: $message")
            DEBUG -> log.debug("v: $message")
            else -> when {
                level == ERROR || warningsAsErrors -> log.error("e: $message")
                else -> log.warn("w: $message")
            }
        }
    }
}