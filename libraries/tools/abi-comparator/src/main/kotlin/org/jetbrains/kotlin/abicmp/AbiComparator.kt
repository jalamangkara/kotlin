package org.jetbrains.kotlin.abicmp

import org.jetbrains.kotlin.abicmp.tasks.DirTask
import org.jetbrains.kotlin.abicmp.tasks.checkerConfiguration
import java.io.File

fun main() {
    checkArrowCore()
}


private fun checkArrowCore() {
    val dir1 = "/Users/vladislav.grechko/Desktop/jar1"
    val dir2 = "/Users/vladislav.grechko/Desktop/jar2"
    val id1: String? = null
    val id2: String? = null
    val reportPath = "/Users/vladislav.grechko/Desktop/report"

    val header1 = "JVM"
    val header2 = "JVM_IR"

    val checkerConfiguration = checkerConfiguration {}

    val reportDir = File(reportPath)
    reportDir.deleteRecursively()
    reportDir.mkdirs()

    println("Checkers:")
    println(checkerConfiguration.enabledCheckers.joinToString(separator = "\n") { " * ${it.name}" })

    DirTask(File(dir1), File(dir2), id1, id2, header1, header2, reportDir, checkerConfiguration).run()
}
