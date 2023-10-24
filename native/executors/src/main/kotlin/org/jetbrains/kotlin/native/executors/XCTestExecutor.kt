/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.native.executors

import org.jetbrains.kotlin.konan.target.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files

abstract class AbstractXCTestExecutor(
    private val configurables: AppleConfigurables,
    private val executor: Executor
) : Executor {
    private val hostExecutor = HostExecutor()

    private val target by configurables::target

    private fun targetPlatform(): String {
        val xcodeTarget = when (target) {
            KonanTarget.MACOS_X64, KonanTarget.MACOS_ARM64 -> "macosx"
            KonanTarget.IOS_X64, KonanTarget.IOS_SIMULATOR_ARM64 -> "iphonesimulator"
            KonanTarget.IOS_ARM64 -> "iphoneos"
            else -> error("Target $target is not supported buy the executor")
        }

        val stdout = ByteArrayOutputStream()
        val request = ExecuteRequest(
            "/usr/bin/xcrun",
            args = mutableListOf("--sdk", xcodeTarget, "--show-sdk-platform-path"),
            stdout = stdout
        )
        hostExecutor.execute(request).assertSuccess()

        return stdout.toString("UTF-8").trim()
    }

    private val frameworkPath: String
        get() = "${targetPlatform()}/Developer/Library/Frameworks/"

    private val xcTestExecutablePath: String
        get() = "${targetPlatform()}/Developer/Library/Xcode/Agents/xctest"

    override fun execute(request: ExecuteRequest): ExecuteResponse {
        val originalBundle = File(request.executableAbsolutePath)
        val bundleToExecute = if (request.args.isNotEmpty()) {
            // Copy the bundle to a temp dir
            val dir = Files.createTempDirectory("tmp-xctest-runner")
            val newBundleFile = originalBundle.run {
                val newPath = dir.resolve(name)
                copyRecursively(newPath.toFile())
                newPath.toFile()
            }
            check(newBundleFile.exists())

            // Passing arguments to the XCTest-runner using Info.plist file.
            val infoPlist = newBundleFile.walk()
                .firstOrNull { it.name == "Info.plist" }
                ?.absolutePath
            checkNotNull(infoPlist) { "Info.plist of xctest-bundle wasn't found. Check the bundle contents and location "}

            val writeArgsRequest = ExecuteRequest(
                executableAbsolutePath = "/usr/libexec/PlistBuddy",
                args = mutableListOf("-c", "Add :KotlinNativeTestArgs string ${request.args.joinToString(" ")}", infoPlist)
            )
            val writeResponse = hostExecutor.execute(writeArgsRequest)
            writeResponse.assertSuccess()

            newBundleFile
        } else {
            originalBundle
        }

        val response = executor.execute(request.copying {
            environment["DYLD_FRAMEWORK_PATH"] = frameworkPath
            executableAbsolutePath = xcTestExecutablePath
            args.clear()
            args.add(bundleToExecute.absolutePath)
        })

        if (bundleToExecute != originalBundle) {
            bundleToExecute.apply {
                // Remove the copied bundle after the run
                deleteRecursively()
                // Also remove the temp directory that contained this bundle
                parentFile.delete()
            }
        }
        return response
    }
}

class XCTestHostExecutor(configurables: AppleConfigurables) : AbstractXCTestExecutor(configurables, HostExecutor())

class XCTestSimulatorExecutor(configurables: AppleConfigurables) :
    AbstractXCTestExecutor(configurables, XcodeSimulatorExecutor(configurables))