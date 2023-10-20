@file:Suppress("UNUSED_VARIABLE", "NAME_SHADOWING")
import org.gradle.api.internal.component.SoftwareComponentInternal
import org.gradle.api.internal.component.UsageContext
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.js.KotlinWasmTargetAttribute
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import org.jetbrains.kotlin.gradle.tasks.UsesKotlinJavaToolchain
import plugins.configureDefaultPublishing
import plugins.configureKotlinPomAttributes
import kotlin.io.path.copyTo

plugins {
    id("kotlin-multiplatform")
    `maven-publish`
    signing
}

description = "Kotlin Standard Library"

configureJvmToolchain(JdkMajorVersion.JDK_1_8)

fun resolvingConfiguration(name: String, configure: Action<Configuration> = Action {}) =
    configurations.create(name) {
        isCanBeResolved = true
        isCanBeConsumed = false
        configure(this)
    }
fun outgoingConfiguration(name: String, configure: Action<Configuration> = Action {}) =
    configurations.create(name) {
        isCanBeResolved = false
        isCanBeConsumed = true
        configure(this)
    }

val configurationBuiltins = resolvingConfiguration("builtins") {
    attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.objects.named(LibraryElements.JAR))
}
dependencies {
    configurationBuiltins(project(":core:builtins"))
}

val builtinsDir = "${rootDir}/core/builtins"
val builtinsSrcDir = "${buildDir}/src/builtin-sources"

val jsCommonDir = "${projectDir}/js"
val jsCommonSrcDir = "${jsCommonDir}/src"
val jsCommonTestSrcDir = "${jsCommonDir}/test"

// for js-ir
val jsIrDir = "${projectDir}/js-ir"
val jsIrMainSources = "${buildDir}/src/jsMainSources"
lateinit var jsIrTarget: KotlinJsTargetDsl

val commonOptIns = listOf(
    "kotlin.ExperimentalMultiplatform",
    "kotlin.contracts.ExperimentalContracts",
)
val commonTestOptIns = listOf(
    "kotlin.ExperimentalUnsignedTypes",
    "kotlin.ExperimentalStdlibApi",
    "kotlin.io.encoding.ExperimentalEncodingApi",
)

kotlin {
    metadata {
        compilations {
            all {
                compileTaskProvider.configure {
                    kotlinOptions {
                        freeCompilerArgs = listOf(
                            "-Xallow-kotlin-package",
                            "-module-name", "kotlin-stdlib-common",
                            "-Xexpect-actual-classes",
                        )
                    }
                    // workaround for compiling legacy MPP metadata, remove when this compilation is not needed anymore
                    // restate the list of opt-ins
                    compilerOptions.optIn.addAll(commonOptIns)
                }
            }
        }
    }
    jvm {
        withJava()
        compilations {
            val compileOnlyDeclarations by creating {
                compileTaskProvider.configure {
                    kotlinOptions {
                        freeCompilerArgs = listOf("-Xallow-kotlin-package")
                    }
                }
            }

            val main by getting {
                compileTaskProvider.configure {
                    this as UsesKotlinJavaToolchain
                    kotlinJavaToolchain.toolchain.use(getToolchainLauncherFor(JdkMajorVersion.JDK_1_6))
                    kotlinOptions {
                        moduleName = "kotlin-stdlib"
                        jvmTarget = "1.8"
                        // providing exhaustive list of args here
                        freeCompilerArgs = listOf(
                            "-Xallow-kotlin-package",
                            "-Xexpect-actual-classes",
                            "-Xmultifile-parts-inherit",
                            "-Xuse-14-inline-classes-mangling-scheme",
                            "-Xbuiltins-from-sources",
                            "-Xno-new-java-annotation-targets",
                        )
                    }
                }
                defaultSourceSet {
                    dependencies {
                        compileOnly(compileOnlyDeclarations.output.allOutputs)
                    }
                }
            }
            val mainJdk7 by creating {
                associateWith(main)
                compileTaskProvider.configure {
                    this as UsesKotlinJavaToolchain
                    kotlinJavaToolchain.toolchain.use(getToolchainLauncherFor(JdkMajorVersion.JDK_1_7))
                    kotlinOptions {
                        moduleName = "kotlin-stdlib-jdk7"
                        jvmTarget = "1.8"
                        freeCompilerArgs = listOf(
                            "-Xallow-kotlin-package",
                            "-Xmultifile-parts-inherit",
                            "-Xno-new-java-annotation-targets"
                        )
                    }
                }
            }
            val mainJdk8 by creating {
                associateWith(main)
                associateWith(mainJdk7)
                compileTaskProvider.configure {
                    kotlinOptions {
                        moduleName = "kotlin-stdlib-jdk8"
                        freeCompilerArgs = listOf(
                            "-Xallow-kotlin-package",
                            "-Xmultifile-parts-inherit",
                            "-Xno-new-java-annotation-targets"
                        )
                    }
                }
            }
            project.sourceSets.create("java9") {
                java.srcDir("jvm/java9")
            }
            configureJava9Compilation("kotlin.stdlib", listOf(
                main.output.allOutputs,
                mainJdk7.output.allOutputs,
                mainJdk8.output.allOutputs,
            ), main.configurations.compileDependencyConfiguration)
            val test by getting {
                associateWith(mainJdk7)
                associateWith(mainJdk8)
                compileTaskProvider.configure {
                    kotlinOptions {
                        freeCompilerArgs += listOf(
                            "-Xallow-kotlin-package", // TODO: maybe rename test packages
                            "-Xexpect-actual-classes",
                        )
                        if (kotlinBuildProperties.useFir) {
                            freeCompilerArgs += "-Xuse-k2"
                        }
                        // This is needed for JavaTypeTest; typeOf for non-reified type parameters doesn't work otherwise, for implementation reasons.
                        freeCompilerArgs -= "-Xno-optimized-callable-references"
                    }
                    doFirst {
                        (this as KotlinJvmCompile).libraries.forEach(::println)
                    }
                }
            }
            val longRunningTest by creating {
                associateWith(main)
                associateWith(mainJdk7)
                associateWith(mainJdk8)
            }
        }
    }
    jsIrTarget = js(IR) {
        if (!kotlinBuildProperties.isTeamcityBuild) {
            browser {}
        }
        nodejs {
            testTask {
                useMocha {
                    timeout = "10s"
                }
            }
        }
        compilations {
            all {
                kotlinOptions {
                    freeCompilerArgs += "-Xallow-kotlin-package"
                }
            }
            val main by getting
            main.apply {
                kotlinOptions {
                    freeCompilerArgs += listOf(
                        "-Xir-module-name=kotlin",
                        "-Xexpect-actual-classes",
                    )

                    if (!kotlinBuildProperties.disableWerror) {
                        allWarningsAsErrors = true
                    }
                }
            }
        }
    }

    sourceSets {
        all {
            kotlin.setSrcDirs(emptyList<File>())
        }
        commonMain {
            val prepareCommonSources by tasks.registering {
                dependsOn(":prepare:build.version:writeStdlibVersion")
            }
            kotlin {
                srcDir("common/src")
                srcDir(files("src").builtBy(prepareCommonSources))
                srcDir("unsigned/src")
                if (!kotlinBuildProperties.isInIdeaSync) {
                    srcDir("$builtinsDir/src/kotlin/internal")
                }
                if (kotlinBuildProperties.isInIdeaSync) {
                    // required for correct resolution of builtin classes in common code in K2 IDE
                    srcDir("$builtinsDir/src")
                }
            }
        }
        commonTest {
            dependencies {
                // TODO: use project dependency when kotlin-test is migrated
                api(project(":kotlin-test:kotlin-test-mpp"))
//                compileOnly("org.jetbrains.kotlin:kotlin-test-common:$bootstrapKotlinVersion")
//                compileOnly("org.jetbrains.kotlin:kotlin-test-annotations-common:$bootstrapKotlinVersion")
//                compileOnly(project(":kotlin-test:kotlin-test-common"))
//                compileOnly(project(":kotlin-test:kotlin-test-annotations-common"))
            }
            kotlin {
                srcDir("common/test")
                srcDir("test")
            }
        }
        val jvmCompileOnlyDeclarations by getting {
            kotlin.srcDir("jvm/compileOnly")
        }
        val jvmMain by getting {
            project.configurations.getByName("jvmMainCompileOnly").extendsFrom(configurationBuiltins)
            dependencies {
                api("org.jetbrains:annotations:13.0")
            }
            val jvmSrcDirs = listOfNotNull(
                "jvm/src",
                "jvm/runtime",
                "$builtinsDir/src".takeUnless { kotlinBuildProperties.isInIdeaSync }
            )
            project.sourceSets["main"].java.srcDirs(*jvmSrcDirs.toTypedArray())
            kotlin.setSrcDirs(jvmSrcDirs)
        }

        val jvmMainJdk7 by getting {
            kotlin.srcDir("jdk7/src")
        }
        val jvmMainJdk8 by getting {
            kotlin.srcDir("jdk8/src")
        }

        val jvmTest by getting {
            languageSettings {
                optIn("kotlin.io.path.ExperimentalPathApi")
            }
            dependencies {
                api(project(":kotlin-test:kotlin-test-mpp", configuration = "jvmJUnitRuntimeElements"))
            }
            kotlin.srcDir("jvm/test")
            kotlin.srcDir("jdk7/test")
            kotlin.srcDir("jdk8/test")
        }

        val jvmLongRunningTest by getting {
            dependencies {
                api(project(":kotlin-test:kotlin-test-mpp", configuration = "jvmJUnitRuntimeElements"))
//                api(project(":kotlin-test:kotlin-test-junit"))
            }
            kotlin.srcDir("jvm/testLongRunning")
        }

        val jsMain by getting {
            val prepareJsIrMainSources by tasks.registering(Sync::class)
            kotlin {
                srcDir(prepareJsIrMainSources)
                srcDir("$jsIrDir/builtins")
                srcDir("$jsIrDir/runtime")
                srcDir("$jsIrDir/src")
            }

            prepareJsIrMainSources.configure {
                val unimplementedNativeBuiltIns =
                    (file("$builtinsDir/native/kotlin/").list()!!.toSortedSet() - file("$jsIrDir/builtins/").list()!!)
                        .map { "core/builtins/native/kotlin/$it" }

                // TODO: try to reuse absolute paths defined in the beginning
                val sources = listOf(
                    "core/builtins/src/kotlin/",
                    "libraries/stdlib/js/src/",
                    "libraries/stdlib/js/runtime/"
                ) + unimplementedNativeBuiltIns

                val excluded = listOf(
                    // stdlib/js/src/generated is used exclusively for current `js-v1` backend.
                    "libraries/stdlib/js/src/generated/**",
                    "libraries/stdlib/js/src/kotlin/browser",
                    "libraries/stdlib/js/src/kotlin/dom",
                    "libraries/stdlib/js/src/org.w3c",
                    "libraries/stdlib/js/src/kotlinx",

                    // JS-specific optimized version of emptyArray() already defined
                    "core/builtins/src/kotlin/ArrayIntrinsics.kt",
                    // included in common
                    "core/builtins/src/kotlin/internal/**",
                )

                sources.forEach { path ->
                    from("$rootDir/$path") {
                        into(path.dropLastWhile { it != '/' })
                        excluded.filter { it.startsWith(path) }.forEach {
                            exclude(it.substring(path.length))
                        }
                    }
                }

                into(jsIrMainSources)

// Required to compile native builtins with the rest of runtime
                val builtInsHeader = """@file:Suppress(
    "NON_ABSTRACT_FUNCTION_WITH_NO_BODY",
    "MUST_BE_INITIALIZED_OR_BE_ABSTRACT",
    "EXTERNAL_TYPE_EXTENDS_NON_EXTERNAL_TYPE",
    "PRIMARY_CONSTRUCTOR_DELEGATION_CALL_EXPECTED",
    "WRONG_MODIFIER_TARGET",
    "UNUSED_PARAMETER"
)
"""
                doLast {
                    unimplementedNativeBuiltIns.forEach { path ->
                        val file = File("$destinationDir/$path")
                        val sourceCode = builtInsHeader + file.readText()
                        file.writeText(sourceCode)
                    }
                }
            }
        }
        val jsTest by getting {
            dependencies {
//                api(project(":kotlin-test:kotlin-test-js-ir"))
            }
            kotlin.srcDir(jsCommonTestSrcDir)
        }

        all sourceSet@ {
            languageSettings {
                // TODO: progressiveMode = use build property 'test.progressive.mode'
                if (this@sourceSet == jvmCompileOnlyDeclarations) {
                    return@languageSettings
                }
                commonOptIns.forEach { optIn(it) }
                if (this@sourceSet.name.endsWith("Test")) {
                    commonTestOptIns.forEach { optIn(it) }
                }
            }
        }
    }
}

dependencies {
    val jvmMainApi by configurations.getting
    val commonMainMetadataElementsWithClassifier by configurations.creating
    val metadataApiElements by configurations.getting
    val nativeApiElements by configurations.creating
    constraints {
        // there is no dependency anymore from kotlin-stdlib to kotlin-stdlib-common,
        // but use this constraint to align it if another library brings it transitively
        jvmMainApi(project(":kotlin-stdlib-common"))
        commonMainMetadataElementsWithClassifier(project(":kotlin-stdlib-common"))
        metadataApiElements(project(":kotlin-stdlib-common"))
        nativeApiElements(project(":kotlin-stdlib-common"))
        // to avoid split package and duplicate classes on classpath after moving them from these artifacts in 1.8.0
        jvmMainApi("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.8.0")
        jvmMainApi("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.0")
    }
}

tasks {
    val metadataJar by existing(Jar::class) {
        archiveAppendix.set("metadata")
    }
    val sourcesJar by existing(Jar::class) {
        archiveAppendix.set("metadata")
    }
    val jvmJar by existing(Jar::class) {
        dependsOn(configurationBuiltins)
        duplicatesStrategy = DuplicatesStrategy.FAIL
        archiveAppendix.set(null as String?)
        manifestAttributes(manifest, "Main", multiRelease = true)
        manifest.attributes(mapOf("Implementation-Title" to "kotlin-stdlib"))
        from { zipTree(configurationBuiltins.singleFile) }
        from(kotlin.jvm().compilations["mainJdk7"].output.allOutputs)
        from(kotlin.jvm().compilations["mainJdk8"].output.allOutputs)
        from(project.sourceSets["java9"].output)
    }

    val jvmSourcesJar by existing(Jar::class) {
        duplicatesStrategy = DuplicatesStrategy.FAIL
        archiveAppendix.set(null as String?)
        into("jvmMain") {
            from("${rootDir}/core/builtins/native")
            from(kotlin.sourceSets["jvmMainJdk7"].kotlin) {
                into("jdk7")
            }
            from(kotlin.sourceSets["jvmMainJdk8"].kotlin) {
                into("jdk8")
            }
        }
    }

    dexMethodCount {
        from(jvmJar)
        ownPackages.set(listOf("kotlin"))
    }

    val jsJar by existing(Jar::class) {
        manifestAttributes(manifest, "Main")
        manifest.attributes(mapOf("Implementation-Title" to "kotlin-stdlib-js"))
    }

    val jsJarForTests by registering(Copy::class) {
        from(jsJar)
        rename { _ -> "full-runtime.klib" }
        // some tests expect stdlib-js klib in this location
        into(rootProject.buildDir.resolve("js-ir-runtime"))
    }

    val jsLegacyJar by registering(Jar::class) {
        archiveAppendix.set("js")
        val jsJarFile = jsJar.get().archiveFile
        inputs.file(jsJarFile)
        doLast {
            jsJarFile.get().asFile.toPath().copyTo(archiveFile.get().asFile.toPath(), overwrite = true)
        }
    }

    val jsRearrangedSourcesJar by registering(Jar::class) {
        archiveClassifier.set("js-sources")
        archiveVersion.set("")
        destinationDirectory.set(file("$buildDir/lib"))

        includeEmptyDirs = false
        duplicatesStrategy = DuplicatesStrategy.FAIL

        into("commonMain") {
            from(kotlin.sourceSets.commonMain.get().kotlin)
        }
        into("jsMain") {
            from(kotlin.sourceSets["jsMain"].kotlin) {
                // just to depend on source-generating tasks
                exclude("**")
            }
            from("${rootDir}/core/builtins/native/kotlin") {
                into("kotlin")
                include("Comparable.kt")
                include("Enum.kt")
            }
            from("$jsIrMainSources/core/builtins/native") {
                exclude("kotlin/Comparable.kt")
            }
            from("$jsIrMainSources/core/builtins/src")
            from("$jsIrMainSources/libraries/stdlib/js/src")
            from("$jsIrDir/builtins") {
                into("kotlin")
                exclude("Enum.kt")
            }
            from("$jsIrDir/runtime") {
                into("runtime")
            }
            from("$jsIrDir/src") {
                include("**/*.kt")
            }
        }
    }

    val jsSourcesJar by existing(Jar::class) {
        val jsSourcesJarFile = jsRearrangedSourcesJar.get().archiveFile
        inputs.file(jsSourcesJarFile)
        doLast {
            jsSourcesJarFile.get().asFile.toPath().copyTo(archiveFile.get().asFile.toPath(), overwrite = true)
        }
    }

    artifacts {
        val distJsJar = configurations.create("distJsJar")
        val distJsSourcesJar = configurations.create("distJsSourcesJar")
        val distJsKlib = configurations.create("distJsKlib")

        add(distJsJar.name, jsLegacyJar)
        add(distJsSourcesJar.name, jsSourcesJar)
        add(distJsKlib.name, jsJar)
    }


    val jvmTest by existing(Test::class)

    listOf(JdkMajorVersion.JDK_9_0, JdkMajorVersion.JDK_10_0, JdkMajorVersion.JDK_11_0).forEach { jvmVersion ->
        val jvmVersionTest = register("jvm${jvmVersion.majorVersion}Test", Test::class) {
            group = "verification"
            javaLauncher.set(getToolchainLauncherFor(jvmVersion))
            // additional test tasks are not configured automatically same as the main test task
            // after KMP plugin stopped applying java plugin
            classpath = jvmTest.get().classpath
            testClassesDirs = jvmTest.get().testClassesDirs

        }
        check.configure { dependsOn(jvmVersionTest) }
    }

    val jvmLongRunningTest by registering(Test::class) {
        group = "verification"
        val compilation = kotlin.jvm().compilations["longRunningTest"]
        classpath = compilation.compileDependencyFiles + compilation.runtimeDependencyFiles + compilation.output.allOutputs
        testClassesDirs = compilation.output.classesDirs
    }

    if (project.hasProperty("kotlin.stdlib.test.long.running")) {
        check.configure { dependsOn(jvmLongRunningTest) }
    }

    /*
    We are using a custom 'kotlin-project-structure-metadata' to ensure 'nativeApiElements' lists 'commonMain' as source set
    */
    val generateProjectStructureMetadata by existing {
        val outputFile = file("build/kotlinProjectStructureMetadata/kotlin-project-structure-metadata.json")
        val outputTestFile = file("kotlin-project-structure-metadata.beforePatch.json")
        val patchedFile = file("kotlin-project-structure-metadata.json")

        inputs.file(patchedFile)
        inputs.file(outputTestFile)

        doLast {
            /*
            Check that the generated 'outputFile' by default matches our expectations stored in the .beforePatch file
            This will fail if the kotlin-project-structure-metadata.json file would change unnoticed (w/o updating our patched file)
             */
            run {
                val outputFileText = outputFile.readText().trim()
                val expectedFileContent = outputTestFile.readText().trim()
                if (outputFileText != expectedFileContent)
                    error(
                        "${outputFile.path} file content does not match expected content\n\n" +
                                "expected:\n\n$expectedFileContent\n\nactual:\n\n$outputFileText"
                    )
            }

            patchedFile.copyTo(outputFile, overwrite = true)
        }
    }

}

// republishing artifacts from kotlin-stdlib-wasm-* projects
// TODO: replace with wasm targets compilation in this project
fun wasmOutgoingConfigurations(target: KotlinWasmTargetAttribute) {
    val targetName = target.toString().replaceFirstChar { it.uppercase() }
    val klib = resolvingConfiguration("wasm${targetName}Klib")
    val sources = resolvingConfiguration("wasm${targetName}Sources")
    dependencies {
        klib(project(":kotlin-stdlib-wasm-$target", configuration = "wasmRuntimeElements"))
        sources(project(":kotlin-stdlib-wasm-$target", configuration = "wasmSourcesElements"))
    }
    listOf(KotlinUsages.KOTLIN_API, KotlinUsages.KOTLIN_RUNTIME).map { usage ->
        val name = usage.substringAfter("kotlin-")
        val configuration = outgoingConfiguration("wasm${targetName}${name.replaceFirstChar { it.uppercase() }}Elements") {
            attributes {
                attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
                attribute(TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE, objects.named("non-jvm"))
                attribute(Usage.USAGE_ATTRIBUTE, objects.named(usage))
                attribute(KotlinPlatformType.attribute, KotlinPlatformType.wasm)
                attribute(KotlinWasmTargetAttribute.wasmTargetAttribute, target)
            }
        }
        artifacts.add(configuration.name, provider { klib.singleFile }) {
            builtBy(klib)
        }
    }
    val outSources = outgoingConfiguration("wasm${targetName}SourcesElements") {
        attributes {
            attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.DOCUMENTATION))
            attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.SOURCES))
            attribute(TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE, objects.named("non-jvm"))
            attribute(Usage.USAGE_ATTRIBUTE, objects.named(KotlinUsages.KOTLIN_RUNTIME))
            attribute(KotlinPlatformType.attribute, KotlinPlatformType.wasm)
            attribute(KotlinWasmTargetAttribute.wasmTargetAttribute, target)
        }
    }
    artifacts.add(outSources.name, provider { sources.singleFile }) {
        builtBy(sources)
    }
}

wasmOutgoingConfigurations(KotlinWasmTargetAttribute.js)
wasmOutgoingConfigurations(KotlinWasmTargetAttribute.wasi)


// region ==== Publishing ====

configureDefaultPublishing()

open class ComponentsFactoryAccess
@javax.inject.Inject
constructor(val factory: SoftwareComponentFactory)

val componentFactory = objects.newInstance<ComponentsFactoryAccess>().factory

val emptyJavadocJar by tasks.creating(org.gradle.api.tasks.bundling.Jar::class) {
    archiveClassifier.set("javadoc")
}

publishing {
    val artifactBaseName = base.archivesName.get()
    configureMultiModuleMavenPublishing {
        val rootModule = module("rootModule") {
            mavenPublication {
                artifactId = artifactBaseName
                configureKotlinPomAttributes(project, "Kotlin Standard Library")
                artifact(emptyJavadocJar)
            }

            // creates a variant from existing configuration or creates new one
            variant("jvmApiElements")
            variant("jvmRuntimeElements")
            variant("jvmSourcesElements")

            variant("metadataApiElements")
            variant("commonMainMetadataElementsWithClassifier") {
                name = "commonMainMetadataElements"
                configuration {
                    isCanBeConsumed = false
                }
                attributes {
                    copyAttributes(from = project.configurations["commonMainMetadataElements"].attributes, to = this)
                }
                artifact(tasks["metadataJar"]) {
                    classifier = "common"
                }
            }
            variant("metadataSourcesElementsFromJvm") {
                name = "metadataSourcesElements"
                configuration {
                    // to avoid clash in Gradle 8+ with metadataSourcesElements configuration with the same attributes
                    isCanBeConsumed = false
                }
                attributes {
                    copyAttributes(from = project.configurations["metadataSourcesElements"].attributes, to = this)
                }
                artifact(tasks["sourcesJar"]) {
                    classifier = "common-sources"
                }
            }
            variant("nativeApiElements") {
                attributes {
                    attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
                    attribute(TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE, objects.named("non-jvm"))
                    attribute(Usage.USAGE_ATTRIBUTE, objects.named(KotlinUsages.KOTLIN_API))
                    attribute(KotlinPlatformType.attribute, KotlinPlatformType.native)
                }
            }
        }

        // we cannot publish legacy common artifact with metadata in kotlin-stdlib-common
        // because it will cause problems in explicitly configured stdlib dependencies in project
//        val common = module("commonModule") {
//            mavenPublication {
//                artifactId = "$artifactBaseName-common"
//                configureKotlinPomAttributes(project, "Kotlin Common Standard Library (for compatibility with legacy multiplatform)")
//                artifact(tasks["sourcesJar"]) // publish sources.jar just for maven, without including it in Gradle metadata
//            }
//            variant("commonMainMetadataElements")
//        }
        val js = module("jsModule") {
            mavenPublication {
                artifactId = "$artifactBaseName-js"
                configureKotlinPomAttributes(project, "Kotlin Standard Library for JS", packaging = "klib")
                artifact(tasks.named("jsLegacyJar"))
            }
            variant("jsApiElements")
            variant("jsRuntimeElements")
            variant("jsSourcesElements")
        }

        val wasmJs = module("wasmJsModule") {
            mavenPublication {
                artifactId = "$artifactBaseName-wasm-js"
                configureKotlinPomAttributes(project, "Kotlin Standard Library for experimental WebAssembly JS platform", packaging = "klib")
            }
            variant("wasmJsApiElements")
            variant("wasmJsRuntimeElements")
            variant("wasmJsSourcesElements")
        }
        val wasmWasi = module("wasmWasiModule") {
            mavenPublication {
                artifactId = "$artifactBaseName-wasm-wasi"
                configureKotlinPomAttributes(project, "Kotlin Standard Library for experimental WebAssembly WASI platform", packaging = "klib")
            }
            variant("wasmWasiApiElements")
            variant("wasmWasiRuntimeElements")
            variant("wasmWasiSourcesElements")
        }

        // Makes all variants from accompanying artifacts visible through `available-at`
        rootModule.include(js, wasmJs, wasmWasi)
    }

    publications {
        val rootModule by existing(MavenPublication::class)
        val jsModule by existing(MavenPublication::class)
        configureSbom("Main", "kotlin-stdlib", setOf("jvmRuntimeClasspath"), rootModule)
        configureSbom("Js", "kotlin-stdlib-js", setOf("jsRuntimeClasspath"), jsModule)

        val wasmJsModule by existing(MavenPublication::class)
        val wasmWasiModule by existing(MavenPublication::class)
        // an arbitrary empty classpath configuration is used for the following sboms
        // TODO: replace with classpath configurations of the corresponding target compilations when they are migrated here (though empty as well)
        configureSbom("Wasm-Js", "kotlin-stdlib-wasm-js", setOf("metadataCompileClasspath"), wasmJsModule)
        configureSbom("Wasm-Wasi", "kotlin-stdlib-wasm-wasi", setOf("metadataCompileClasspath"), wasmWasiModule)
    }
}

fun copyAttributes(from: AttributeContainer, to: AttributeContainer,) {
    // capture type argument T
    fun <T : Any> copyOneAttribute(from: AttributeContainer, to: AttributeContainer, key: Attribute<T>) {
        val value = checkNotNull(from.getAttribute(key))
        to.attribute(key, value)
    }
    for (key in from.keySet()) {
        copyOneAttribute(from, to, key)
    }
}

class MultiModuleMavenPublishingConfiguration() {
    val modules = mutableMapOf<String, Module>()

    class Module(val name: String) {
        val variants = mutableMapOf<String, Variant>()
        val includes = mutableSetOf<Module>()

        class Variant(
            val configurationName: String
        ) {
            var name: String = configurationName
            val attributesConfigurations = mutableListOf<AttributeContainer.() -> Unit>()
            fun attributes(code: AttributeContainer.() -> Unit) {
                attributesConfigurations += code
            }

            val artifactsWithConfigurations = mutableListOf<Pair<Any, ConfigurablePublishArtifact.() -> Unit>>()
            fun artifact(file: Any, code: ConfigurablePublishArtifact.() -> Unit = {}) {
                artifactsWithConfigurations += file to code
            }

            val configurationConfigurations = mutableListOf<Configuration.() -> Unit>()
            fun configuration(code: Configuration.() -> Unit) {
                configurationConfigurations += code
            }

            val variantDetailsConfigurations = mutableListOf<ConfigurationVariantDetails.() -> Unit>()
            fun configureVariantDetails(code: ConfigurationVariantDetails.() -> Unit) {
                variantDetailsConfigurations += code
            }
        }

        val mavenPublicationConfigurations = mutableListOf<MavenPublication.() -> Unit>()
        fun mavenPublication(code: MavenPublication.() -> Unit) {
            mavenPublicationConfigurations += code
        }

        fun variant(fromConfigurationName: String, code: Variant.() -> Unit = {}): Variant {
            val variant = variants.getOrPut(fromConfigurationName) { Variant(fromConfigurationName) }
            variant.code()
            return variant
        }

        fun include(vararg modules: Module) {
            includes.addAll(modules)
        }
    }

    fun module(name: String, code: Module.() -> Unit): Module {
        val module = modules.getOrPut(name) { Module(name) }
        module.code()
        return module
    }
}

fun configureMultiModuleMavenPublishing(code: MultiModuleMavenPublishingConfiguration.() -> Unit) {
    val publishingConfiguration = MultiModuleMavenPublishingConfiguration()
    publishingConfiguration.code()

    val components = publishingConfiguration
        .modules
        .mapValues { (_, module) -> project.createModulePublication(module) }

    val componentsWithExternals = publishingConfiguration
        .modules
        .filter { (_, module) -> module.includes.isNotEmpty() }
        .mapValues { (moduleName, module) ->
            val mainComponent = components[moduleName] ?: error("Component with name $moduleName wasn't created")
            val externalComponents = module.includes
                .map { components[it.name] ?: error("Component with name ${it.name} wasn't created") }
                .toSet()
            ComponentWithExternalVariants(mainComponent, externalComponents)
        }

    // override some components wih items from componentsWithExternals
    val mergedComponents = components + componentsWithExternals

    val publicationsContainer = publishing.publications
    for ((componentName, component) in mergedComponents) {
        publicationsContainer.create<MavenPublication>(componentName) {
            from(component)
            val module = publishingConfiguration.modules[componentName]!!
            module.mavenPublicationConfigurations.forEach { configure -> configure() }
        }
    }
}


fun Project.createModulePublication(module: MultiModuleMavenPublishingConfiguration.Module): SoftwareComponent {
    val component = componentFactory.adhoc(module.name)
    module.variants.values.forEach { addVariant(component, it) }

    val newNames = module.variants.map { it.key to it.value.name }.filter { it.first != it.second }.toMap()
    return if (newNames.isNotEmpty()) {
        ComponentWithRenamedVariants(newNames, component as SoftwareComponentInternal)
    } else {
        component
    }
}

fun Project.addVariant(component: AdhocComponentWithVariants, variant: MultiModuleMavenPublishingConfiguration.Module.Variant) {
    val configuration = configurations.getOrCreate(variant.configurationName)
    configuration.apply {
        isCanBeResolved = false
        isCanBeConsumed = true

        variant.attributesConfigurations.forEach { configure -> attributes.configure() }
    }

    for ((artifactNotation, configure) in variant.artifactsWithConfigurations) {
        artifacts.add(configuration.name, artifactNotation) {
            configure()
        }
    }

    for (configure in variant.configurationConfigurations) {
        configuration.apply(configure)
    }

    component.addVariantsFromConfiguration(configuration) {
        variant.variantDetailsConfigurations.forEach { configure -> configure() }
    }
}

private class RenamedVariant(val newName: String, context: UsageContext) : UsageContext by context {
    override fun getName(): String = newName
}

private class ComponentWithRenamedVariants(
    val newNames: Map<String, String>,
    private val base: SoftwareComponentInternal
): SoftwareComponentInternal by base {

    override fun getName(): String = base.name
    override fun getUsages(): Set<UsageContext> {
        return base.usages.map {
            val newName = newNames[it.name]
            if (newName != null) {
                RenamedVariant(newName, it)
            } else {
                it
            }
        }.toSet()
    }
}

private class ComponentWithExternalVariants(
    private val mainComponent: SoftwareComponent,
    private val externalComponents: Set<SoftwareComponent>
) : ComponentWithVariants, SoftwareComponentInternal {
    override fun getName(): String = mainComponent.name

    override fun getUsages(): Set<UsageContext> = (mainComponent as SoftwareComponentInternal).usages

    override fun getVariants(): Set<SoftwareComponent> = externalComponents
}

// endregion

// for legacy intra-project dependencies
for (name in listOf("sources", "distSources")) {
    val sourcesConfiguration = configurations.getOrCreate(name).apply {
        isCanBeResolved = false
        isCanBeConsumed = true
    }
    artifacts.add(sourcesConfiguration.name, tasks["jvmSourcesJar"])
}
