/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.generator.print

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import org.jetbrains.kotlin.generators.tree.printer.GeneratedFile
import org.jetbrains.kotlin.generators.tree.printer.printGeneratedType
import org.jetbrains.kotlin.ir.generator.util.Import
import java.io.File

internal const val TREE_GENERATOR_README = "compiler/ir/ir.tree/tree-generator/ReadMe.md"

fun printTypeCommon(
    generationPath: File,
    packageName: String,
    type: TypeSpec,
    additionalImports: List<Import> = emptyList(),
): GeneratedFile = printGeneratedType(generationPath, TREE_GENERATOR_README, packageName, type.name!!) {
    val code = FileSpec.builder(packageName, type.name!!)
        .apply {
            additionalImports.forEach { addImport(it.packageName, it.className) }
        }
        .indent("    ")
        .addType(type)
        .build()
        .toString()
        .replace("package $packageName\n\n", "")
        .unbacktickIdentifiers("data", "value", "operator", "constructor", "delegate", "receiver", "field")
        .replace("public ", "")
        .replace(":\\s*Unit".toRegex(), "")
        .replace("import kotlin\\..*\\n".toRegex(), "")
        // Half-baked attempt to remove double indent generated by KotlinPoet, which is not idiomatic according to the Kotlin style guide
        .replace("            visitor.visit", "        visitor.visit")
        .replace("            accept(transformer, data)", "        accept(transformer, data)")
    print(code)
}

private fun String.unbacktickIdentifiers(vararg identifiers: String): String {
    var result = this
    for (identifier in identifiers) {
        result = result.replace("`$identifier`", identifier)
    }
    return result
}
