@file:Suppress("UNUSED_VARIABLE", "ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
import org.gradle.api.internal.component.SoftwareComponentInternal
import org.gradle.api.internal.component.UsageContext
import org.gradle.api.publish.internal.PublicationInternal
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages
import plugins.configureDefaultPublishing
import plugins.configureKotlinPomAttributes

plugins {
    id("kotlin-multiplatform")
    `maven-publish`
    signing
}

description = "Kotlin Test Library"
base.archivesName = "kotlin-test-mpp"

configureJvmToolchain(JdkMajorVersion.JDK_1_8)

val kotlinTestCapability = "$group:${base.archivesName.get()}:$version" // add to variants with explicit capabilities when the default one is needed, too
val baseCapability = "$group:kotlin-test-framework:$version"
val implCapability = "$group:kotlin-test-framework-impl:$version"

val jvmTestFrameworks = listOf("JUnit", "JUnit5")

kotlin {
    lateinit var jvmMainCompilation: KotlinJvmCompilation
    jvm {
        compilations {
            val main by getting {
            }
            jvmMainCompilation = main
            jvmTestFrameworks.forEach { framework ->
                create(framework) {
                    associateWith(main)
                }
            }
        }
    }
    js {
        if (!kotlinBuildProperties.isTeamcityBuild) {
            browser {}
        }
        nodejs {}
    }

    targets.all {
        compilations.all {
            compilerOptions.configure {
                optIn.add("kotlin.contracts.ExperimentalContracts")
                freeCompilerArgs.addAll(listOf(
                    "-Xallow-kotlin-package",
                    "-Xexpect-actual-classes",
                ))
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlinStdlib())
            }
        }
        val annotationsCommonMain by creating {
            dependsOn(commonMain)
            kotlin.srcDir("../annotations-common/src/main/kotlin")
        }
        val assertionsCommonMain by creating {
            dependsOn(commonMain)
            kotlin.srcDir("../common/src/main/kotlin")
        }
        val jvmMain by getting {
            dependsOn(assertionsCommonMain)
            kotlin.srcDir("../jvm/src/main/kotlin")
        }
        val jvmJUnit by getting {
            dependsOn(annotationsCommonMain)
            kotlin.srcDir("../junit/src/main/kotlin")
            resources.srcDir("../junit/src/main/resources")
            dependencies {
//                api(jvmMainCompilation.output.allOutputs)
                api("junit:junit:4.13.2")
            }
        }
        val jvmJUnit5 by getting {
            dependsOn(annotationsCommonMain)
            kotlin.srcDir("../junit5/src/main/kotlin")
            resources.srcDir("../junit5/src/main/resources")
            dependencies {
//                api(jvmMainCompilation.output.allOutputs)
                compileOnly("org.junit.jupiter:junit-jupiter-api:5.0.0")
                runtimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.3")
            }
        }
        val jsMain by getting {
            dependsOn(assertionsCommonMain)
            dependsOn(annotationsCommonMain)
            kotlin.srcDir("../js/src/main/kotlin")
        }
    }
}


tasks {
    val jvmJar by existing(Jar::class) {
        archiveAppendix = null
        manifestAttributes(manifest, "Test")
    }
    val jvmJarTasks = jvmTestFrameworks.map { framework ->
        register("jvm${framework}Jar", Jar::class) {
            archiveAppendix = framework.lowercase()
            from(kotlin.jvm().compilations[framework].output.allOutputs)
            manifestAttributes(manifest, "Test")
        }
    }
    val jvmSourcesJarTasks = jvmTestFrameworks.forEach { framework ->
        register("jvm${framework}SourcesJar", Jar::class) {
            archiveAppendix = framework.lowercase()
            archiveClassifier = "sources"
            kotlin.jvm().compilations[framework].allKotlinSourceSets.forEach {
                from(it.kotlin.sourceDirectories) { into(it.name) }
                from(it.resources.sourceDirectories) { into(it.name) }
            }
        }
    }
    val assemble by existing {
        dependsOn(jvmJarTasks)
    }

    val generateProjectStructureMetadata by existing {
        val outputFile = file("build/kotlinProjectStructureMetadata/kotlin-project-structure-metadata.json")
        val outputTestFile = file("kotlin-project-structure-metadata.beforePatch.json")
        val patchedFile = file("kotlin-project-structure-metadata.json")

        inputs.file(patchedFile)
//        inputs.file(outputTestFile)

        doLast {
            /*
            Check that the generated 'outputFile' by default matches our expectations stored in the .beforePatch file
            This will fail if the kotlin-project-structure-metadata.json file would change unnoticed (w/o updating our patched file)
             */
            run {
                val outputFileText = outputFile.readText().trim()
//                val expectedFileContent = outputTestFile.readText().trim()
//                if (outputFileText != expectedFileContent)
//                    error(
//                        "${outputFile.path} file content does not match expected content\n\n" +
//                                "expected:\n\n$expectedFileContent\n\nactual:\n\n$outputFileText"
//                    )
            }

            patchedFile.copyTo(outputFile, overwrite = true)
        }
    }

}

configurations {
    val metadataApiElements by getting {
        outgoing.capability(kotlinTestCapability)
    }
    for (framework in jvmTestFrameworks) {
        val frameworkCapability = "$group:kotlin-test-framework-${framework.lowercase()}:$version"
        for (usage in listOf(KotlinUsages.KOTLIN_API, KotlinUsages.KOTLIN_RUNTIME, KotlinUsages.KOTLIN_SOURCES)) {
            for (isInRoot in listOf(true, false)) {
                if (isInRoot && usage == KotlinUsages.KOTLIN_SOURCES) continue
                val name = "jvm$framework${usage.substringAfter("kotlin-").replaceFirstChar { it.uppercase() }}${if (isInRoot) "Elements" else "Elements-published"}"
                create(name) {
                    isCanBeResolved = false
                    isCanBeConsumed = true
                    if (isInRoot) {
                        outgoing.capability(baseCapability)
                        outgoing.capability(frameworkCapability)
                    } else {
//                        attributes {
//                            attribute(Attribute.of("disambiguation", String::class.java), framework)
//                        }
                        outgoing.capability("$group:${base.archivesName.get()}-${framework.lowercase()}:$version")
                        outgoing.capability(implCapability)
                    }
                    attributes {
                        attribute(TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE, objects.named(TargetJvmEnvironment.STANDARD_JVM))
                        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
                        attribute(KotlinPlatformType.attribute, KotlinPlatformType.jvm)
                        attribute(
                            Usage.USAGE_ATTRIBUTE, objects.named(
                                when (usage) {
                                    KotlinUsages.KOTLIN_API -> Usage.JAVA_API
                                    KotlinUsages.KOTLIN_RUNTIME -> Usage.JAVA_RUNTIME
                                    KotlinUsages.KOTLIN_SOURCES -> Usage.JAVA_RUNTIME
                                    else -> error(usage)
                                }
                            )
                        )
                        when (usage) {
                            KotlinUsages.KOTLIN_SOURCES -> {
                                attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.DOCUMENTATION))
                                attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.SOURCES))
                                attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
                            }
                            KotlinUsages.KOTLIN_API -> {
                                attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
                                if (!isInRoot) extendsFrom(getByName("jvm${framework}Api"))
                            }
                            KotlinUsages.KOTLIN_RUNTIME -> {
                                attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
                                if (!isInRoot) extendsFrom(getByName("jvm${framework}Api"))
                                if (!isInRoot) extendsFrom(getByName("jvm${framework}RuntimeOnly"))
                            }
                            else -> error(usage)
                        }
                    }
                }
                if (usage != KotlinUsages.KOTLIN_SOURCES) {
                    dependencies {
                        if (isInRoot) add(name, "$group:${base.archivesName.get()}-${framework.lowercase()}:$version")
                        if (!isInRoot) add(name, project)
                    }
                    artifacts {
                        if (!isInRoot) add(name, tasks.named<Jar>("jvm${framework}Jar"))
                    }
                } else {
                    artifacts {
                        add(name, tasks.named<Jar>("jvm${framework}SourcesJar"))
                    }
                }
            }
            metadataApiElements {
                outgoing.capability(frameworkCapability)
            }
        }
    }
    all {
        println(name)
    }
}


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
                configureKotlinPomAttributes(project, "Kotlin Test Library")
                artifact(emptyJavadocJar)
            }
//
//            // creates a variant from existing configuration or creates new one

            variant("metadataApiElements")
//            variant("commonMainMetadataElementsWithClassifier") {
//                name = "commonMainMetadataElements"
//                configuration {
//                    isCanBeConsumed = false
//                }
//                attributes {
//                    copyAttributes(from = project.configurations["commonMainMetadataElements"].attributes, to = this)
//                }
//                artifact(tasks["metadataJar"]) {
//                    classifier = "common"
//                }
//            }
//            variant("metadataSourcesElementsFromJvm") {
//                name = "metadataSourcesElements"
//                configuration {
//                    // to avoid clash in Gradle 8+ with metadataSourcesElements configuration with the same attributes
//                    isCanBeConsumed = false
//                }
//                attributes {
//                    copyAttributes(from = project.configurations["metadataSourcesElements"].attributes, to = this)
//                }
//                artifact(tasks["sourcesJar"]) {
//                    classifier = "common-sources"
//                }
//            }
            jvmTestFrameworks.forEach { framework ->
                variant("jvm${framework}ApiElements")
                variant("jvm${framework}RuntimeElements")
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
        val jvm = module("jvmModule") {
            mavenPublication {
                artifactId = "$artifactBaseName-jvm"
                configureKotlinPomAttributes(project, "Kotlin Test Library for JVM")
            }
            variant("jvmApiElements")
            variant("jvmRuntimeElements")
            variant("jvmSourcesElements")
        }
        val js = module("jsModule") {
            mavenPublication {
                artifactId = "$artifactBaseName-js"
                configureKotlinPomAttributes(project, "Kotlin Test Library for JS", packaging = "klib")
            }
            variant("jsApiElements")
            variant("jsRuntimeElements")
            variant("jsSourcesElements")
        }
        val frameworkModules = jvmTestFrameworks.map { framework ->
            module("${framework.lowercase()}Module") {
                mavenPublication {
                    artifactId = "$artifactBaseName-${framework.lowercase()}"
                    configureKotlinPomAttributes(project, "Kotlin Test Library for ${framework}")
                    (this as PublicationInternal<*>).isAlias = true
                }
                variant("jvm${framework}ApiElements-published")
                variant("jvm${framework}RuntimeElements-published")
                variant("jvm${framework}SourcesElements-published")
            }
        }


        // Makes all variants from accompanying artifacts visible through `available-at`
        rootModule.include(js, jvm)
    }

//    publications {
//    }
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

//tasks.withType<GenerateModuleMetadata> {
//        // temporary disable Gradle metadata in kotlin-test-junit artifact
//        // until we find a solution for duplicated capabilities
//        if (listOf("junit", "junit5").any { it in (publication.get() as MavenPublication).artifactId }) {
//            enabled = false
//        }
//    }