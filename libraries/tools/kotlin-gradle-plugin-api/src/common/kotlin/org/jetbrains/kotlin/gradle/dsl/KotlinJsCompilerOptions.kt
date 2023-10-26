// DO NOT EDIT MANUALLY!
// Generated by org/jetbrains/kotlin/generators/arguments/GenerateGradleOptions.kt
// To regenerate run 'generateGradleOptions' task
@file:Suppress("RemoveRedundantQualifierName", "Deprecation", "DuplicatedCode")

package org.jetbrains.kotlin.gradle.dsl

interface KotlinJsCompilerOptions : org.jetbrains.kotlin.gradle.dsl.KotlinCommonCompilerOptions {

    /**
     * Disable internal declaration export
     * Default value: false
     */
    @get:org.gradle.api.tasks.Input
    val friendModulesDisabled: org.gradle.api.provider.Property<kotlin.Boolean>

    /**
     * Define whether the `main` function should be called upon execution
     * Possible values: "call", "noCall"
     * Default value: JsMainFunctionExecutionMode.CALL
     */
    @get:org.gradle.api.tasks.Input
    val main: org.gradle.api.provider.Property<org.jetbrains.kotlin.gradle.dsl.JsMainFunctionExecutionMode>

    /**
     * Generate .meta.js and .kjsm files with metadata. Use to create a library
     * Default value: true
     */
    @get:org.gradle.api.tasks.Input
    val metaInfo: org.gradle.api.provider.Property<kotlin.Boolean>

    /**
     * Kind of the JS module generated by the compiler
     * Possible values: "plain", "amd", "commonjs", "umd"
     * Default value: JsModuleKind.MODULE_PLAIN
     */
    @get:org.gradle.api.tasks.Input
    val moduleKind: org.gradle.api.provider.Property<org.jetbrains.kotlin.gradle.dsl.JsModuleKind>

    /**
     * Base name of generated files
     * Default value: null
     */
    @get:org.gradle.api.tasks.Optional
    @get:org.gradle.api.tasks.Input
    val moduleName: org.gradle.api.provider.Property<kotlin.String>

    /**
     * Don't automatically include the default Kotlin/JS stdlib into compilation dependencies
     * Default value: true
     */
    @get:org.gradle.api.tasks.Input
    val noStdlib: org.gradle.api.provider.Property<kotlin.Boolean>

    /**
     * Destination *.js file for the compilation result
     * Default value: null
     */
    @Deprecated(message = "Only for legacy backend. For IR backend please use task.destinationDirectory and moduleName", level = DeprecationLevel.WARNING)
    @get:org.gradle.api.tasks.Internal
    val outputFile: org.gradle.api.provider.Property<kotlin.String>

    /**
     * Define JS expression to get platform specific args as a parameter of the main function
     * Default value: null
     */
    @get:org.gradle.api.tasks.Optional
    @get:org.gradle.api.tasks.Input
    val platformArgumentsProviderJsExpression: org.gradle.api.provider.Property<kotlin.String>

    /**
     * Generate source map
     * Default value: false
     */
    @get:org.gradle.api.tasks.Input
    val sourceMap: org.gradle.api.provider.Property<kotlin.Boolean>

    /**
     * Embed source files into source map
     * Possible values: "never", "always", "inlining"
     * Default value: null
     */
    @get:org.gradle.api.tasks.Optional
    @get:org.gradle.api.tasks.Input
    val sourceMapEmbedSources: org.gradle.api.provider.Property<org.jetbrains.kotlin.gradle.dsl.JsSourceMapEmbedMode>

    /**
     * How to map generated names to original names (IR backend only)
     * Possible values: "no", "simple-names", "fully-qualified-names"
     * Default value: null
     */
    @get:org.gradle.api.tasks.Optional
    @get:org.gradle.api.tasks.Input
    val sourceMapNamesPolicy: org.gradle.api.provider.Property<org.jetbrains.kotlin.gradle.dsl.JsSourceMapNamesPolicy>

    /**
     * Add the specified prefix to paths in the source map
     * Default value: null
     */
    @get:org.gradle.api.tasks.Optional
    @get:org.gradle.api.tasks.Input
    val sourceMapPrefix: org.gradle.api.provider.Property<kotlin.String>

    /**
     * Generate JS files for specific ECMA version
     * Possible values: "v5"
     * Default value: "v5"
     */
    @get:org.gradle.api.tasks.Input
    val target: org.gradle.api.provider.Property<kotlin.String>

    /**
     * Translate primitive arrays to JS typed arrays
     * Default value: true
     */
    @get:org.gradle.api.tasks.Input
    val typedArrays: org.gradle.api.provider.Property<kotlin.Boolean>

    /**
     * Generated JavaScript will use ES2015 classes.
     * Default value: false
     */
    @get:org.gradle.api.tasks.Input
    val useEsClasses: org.gradle.api.provider.Property<kotlin.Boolean>
}
