/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.backend.handlers

import com.google.common.collect.Sets
import org.jetbrains.kotlin.abicmp.defects.Location
import org.jetbrains.kotlin.abicmp.reports.ClassReport
import org.jetbrains.kotlin.abicmp.reports.DefectReport
import org.jetbrains.kotlin.abicmp.reports.REPORT_CSS
import org.jetbrains.kotlin.abicmp.reports.isNotEmpty
import org.jetbrains.kotlin.abicmp.tag
import org.jetbrains.kotlin.abicmp.tasks.ClassTask
import org.jetbrains.kotlin.abicmp.tasks.checkerConfiguration
import org.jetbrains.kotlin.codegen.getClassFiles
import org.jetbrains.kotlin.test.backend.codegenSuppressionChecker
import org.jetbrains.kotlin.test.backend.ir.mutedAbiChecking
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives.IGNORE_BACKEND_K1
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives.IGNORE_BACKEND_K2
import org.jetbrains.kotlin.test.model.*
import org.jetbrains.kotlin.test.runners.AbiConsistencyTestGenerated
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.testInfo
import org.jetbrains.org.objectweb.asm.ClassReader
import org.jetbrains.org.objectweb.asm.tree.ClassNode
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class AbiConsistencyHandler(testServices: TestServices) : AnalysisHandler<BinaryArtifacts.TwoJvm>(testServices, true, true) {
    override val artifactKind: TestArtifactKind<BinaryArtifacts.TwoJvm>
        get() = ArtifactKinds.TwoJvm

    override fun processAfterAllModules(someAssertionWasFailed: Boolean) {
        // do nothing
    }

    private val suppressionChecker = testServices.codegenSuppressionChecker

    private val TestModule.ignored: Boolean
        get() = suppressionChecker.failuresInModuleAreIgnored(this, IGNORE_BACKEND_K1).testMuted
                || suppressionChecker.failuresInModuleAreIgnored(this, IGNORE_BACKEND_K2).testMuted

    override fun processModule(module: TestModule, info: BinaryArtifacts.TwoJvm) {
        if (module.ignored) return
        val classesFromK1 = info.fromClassicFrontend.classFileFactory.getClassFiles().associate { it.relativePath to it.asByteArray() }
        val classesFromK2 = info.fromFir.classFileFactory.getClassFiles().associate { it.relativePath to it.asByteArray() }
        Sets.difference(classesFromK1.keys, classesFromK2.keys)
            .also { if (it.isNotEmpty()) assertions.fail { "Missing classes in K2: ${assertions.renderCollectionToString(it)}" } }
        Sets.difference(classesFromK2.keys, classesFromK1.keys)
            .also { if (it.isNotEmpty()) assertions.fail { "Missing classes in K1: ${assertions.renderCollectionToString(it)}" } }

        val nonEmptyClassReports = mutableListOf<ClassReport>()
        classesFromK1.keys.forEach { classInternalName ->
            val k1ClassNode = parseClassNode(classesFromK1[classInternalName]!!)
            val k2ClassNode = parseClassNode(classesFromK2[classInternalName]!!)
            val classReport = ClassReport(Location.Class("", classInternalName), classInternalName, "K1", "K2", DefectReport())
            ClassTask(checkerConfiguration { }, k1ClassNode, k2ClassNode, classReport).run()
            if (classReport.isNotEmpty()) {
                nonEmptyClassReports.add(classReport)
            }
        }
        if (nonEmptyClassReports.isNotEmpty()) {
            if (testServices.mutedAbiChecking) {
                assertions.fail { "ABI difference obtained." }
            } else {
                val reportPath = reportToFile(nonEmptyClassReports.asHtmlReport())
                assertions.fail { "ABI difference obtained, see the report: ${reportPath.toAbsolutePath()}" }
            }
        }
    }

    private fun reportToFile(htmlReport: String): Path {
        val path = Paths.get("tmp").resolve("abiCheckerReports")
            .resolve("${testServices.testInfo.className}.${testServices.testInfo.methodName}.html")
        Files.deleteIfExists(path)
        path.parent.toFile().mkdirs()
        Files.write(path, htmlReport.toByteArray())
        return path
    }

    private val filePath: String
        get() {
            val prefix = "compiler/testData/codegen"
            val path =
                testServices.testInfo.className.substring(AbiConsistencyTestGenerated::class.qualifiedName!!.length)
                    .lowercase()
                    .replace('$', '/')
            val methodName = testServices.testInfo.methodName
            val fileName = methodName.substring("test".length).let { it.substring(0, 1).lowercase() + it.substring(1) } + ".kt"
            return "$prefix$path/$fileName"
        }

    private fun List<ClassReport>.asHtmlReport(): String {
        val outputStream = ByteArrayOutputStream()
        PrintWriter(outputStream, true).use { out ->
            out.tag("html") {
                out.tag("head") {
                    out.tag("style", REPORT_CSS)
                }
                out.tag("body") {
                    out.println(filePath)
                    forEach { it.write(out) }
                }
            }
        }
        return outputStream.toString()
    }

    private fun parseClassNode(byteArray: ByteArray) =
        ClassNode().also { ClassReader(ByteArrayInputStream(byteArray)).accept(it, ClassReader.SKIP_CODE) }
}