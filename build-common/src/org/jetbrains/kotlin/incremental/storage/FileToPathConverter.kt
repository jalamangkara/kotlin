/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.incremental.storage

import java.io.File

/** Converts a [File] to a path of type [String] to store in IC caches, and vice versa. */
interface FileToPathConverter {
    fun toPath(file: File): String
    fun toFile(path: String): File
}

object BasicFileToPathConverter : FileToPathConverter {
    override fun toPath(file: File): String = file.path
    override fun toFile(path: String): File = File(path)
}
