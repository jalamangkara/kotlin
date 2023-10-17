import org.jetbrains.kotlin.*
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages
import org.jetbrains.kotlin.gradle.tasks.CInteropProcess
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import org.jetbrains.kotlin.konan.target.*
import java.io.ByteArrayOutputStream

plugins {
    id("kotlin.native.build-tools-conventions")
    kotlin("multiplatform")
}

val distDir: File by project
val konanHome: String by extra(distDir.absolutePath)
// Set native home for KGP
extra["kotlin.native.home"] = konanHome

with(PlatformInfo) {
    if (isMac()) {
        checkXcodeVersion(project)
    }
}

/**
 * Path to the target SDK platform.
 *
 * By default, K/N includes only SDK frameworks.
 * It's required to get the Library frameworks path where the `XCTest.framework` is located.
 */
fun targetPlatform(target: String): String {
    val out = ByteArrayOutputStream()
    val result = project.exec {
        executable = "/usr/bin/xcrun"
        args = listOf("--sdk", target, "--show-sdk-platform-path")
        standardOutput = out
    }

    check(result.exitValue == 0) {
        "xcrun ended unsuccessfully. See the output: $out"
    }

    return out.toString().trim()
}

val targets = listOf(
    KonanTarget.MACOS_X64,
    KonanTarget.MACOS_ARM64,
    KonanTarget.IOS_X64,
    KonanTarget.IOS_SIMULATOR_ARM64,
    KonanTarget.IOS_ARM64
).filter {
    it in platformManager.enabled
}

/*
 * Double laziness: lazily create functions that execute `/usr/bin/xcrun` and return
 * a path to the Developer frameworks.
 */
val developerFrameworks: Map<KonanTarget, () -> String> by lazy {
    platformManager.enabled
        .filter { it.family.isAppleFamily }
        .associateWith { target ->
            val configurable = platformManager.platform(target).configurables as AppleConfigurables
            val platform = configurable.platformName().lowercase()
            fun(): String = "${targetPlatform(platform)}/Developer/Library/Frameworks/"
        }
}

/**
 * Gets a path to the developer frameworks location.
 */
fun KonanTarget.getDeveloperFramework(): String = developerFrameworks[this]?.let { it() } ?: error("Not supported target $this")

if (PlatformInfo.isMac()) {
    kotlin {
        val nativeTargets = listOf(
            macosX64(KonanTarget.MACOS_X64.name),
            macosArm64(KonanTarget.MACOS_ARM64.name),
            iosX64(KonanTarget.IOS_X64.name),
            iosArm64(KonanTarget.IOS_ARM64.name),
            iosSimulatorArm64(KonanTarget.IOS_SIMULATOR_ARM64.name)
        )

        nativeTargets.forEach {
            it.compilations.all {
                // Consider making XCTest a platform lib with KT-61709
                cinterops {
                    create("XCTest") {
                        compilerOpts("-iframework", konanTarget.getDeveloperFramework())
                    }
                }
            }
        }
        sourceSets.all {
            languageSettings.apply {
                // Oh, yeah! So much experimental, so wow!
                optIn("kotlinx.cinterop.BetaInteropApi")
                optIn("kotlinx.cinterop.ExperimentalForeignApi")
                optIn("kotlin.experimental.ExperimentalNativeApi")
            }
        }
    }
}

// Due to KT-42056 and KT-48410, it is not possible to set dependencies on dist when opened in the IDEA.
// IDEA sync makes cinterop tasks eagerly resolve dependencies, effectively running the dist-build in configuration time
if (!project.isIdeaActive) {
    targets.forEach {
        val targetName = it.name.capitalize()
        tasks.named<KotlinNativeCompile>("compileKotlin$targetName") {
            dependsOnDist(it)
            dependsOnPlatformLibs(it)
        }

        tasks.named<CInteropProcess>("cinteropXCTest$targetName") {
            dependsOnDist(it)
            dependsOnPlatformLibs(it)
        }
    }
} else {
    tasks.named("prepareKotlinIdeaImport") {
        enabled = false
    }
}

val XCTestRunnerArtifacts by configurations.creating {
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(KotlinUsages.KOTLIN_API))
        // Native target-specific
        attribute(KotlinPlatformType.attribute, KotlinPlatformType.native)
    }
}

targets.forEach { target ->
    val targetName = target.name
    val outputKlibTask = tasks.named<KotlinNativeCompile>("compileKotlin${targetName.capitalize()}")
    val cinteropKlibTask = tasks.named<CInteropProcess>("cinteropXCTest${targetName.capitalize()}")

    artifacts {
        add(XCTestRunnerArtifacts.name, outputKlibTask.flatMap { it.outputFile }) {
            classifier = targetName
            builtBy(outputKlibTask)
        }
        add(XCTestRunnerArtifacts.name, cinteropKlibTask.flatMap { it.outputFileProvider }) {
            classifier = targetName
            builtBy(cinteropKlibTask)
        }
        add(XCTestRunnerArtifacts.name, File(target.getDeveloperFramework())) {
            classifier = "${targetName}Frameworks"
        }
    }
}
