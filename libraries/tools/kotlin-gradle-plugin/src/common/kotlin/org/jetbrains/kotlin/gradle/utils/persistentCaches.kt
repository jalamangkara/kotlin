/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.utils

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider.Companion.kotlinPropertiesProvider
import java.io.File
import java.security.MessageDigest

private const val PROJECTS_CACHE_NAME = "projects"
private const val PROJECTS_CACHE_VERSION = 1
private const val PROJECTS_CACHE_NAME_FULL = "$PROJECTS_CACHE_NAME-$PROJECTS_CACHE_VERSION"

private const val SESSIONS_DIR_NAME = "sessions"
private const val METADATA_DIR_NAME = "metadata"
private const val ERRORS_DIR_NAME = "errors"

internal val Project.basePersistentDir
    get() = kotlinPropertiesProvider.kotlinUserHomeDir?.let { File(it) }
        ?: File(System.getProperty("user.home")).resolve(".kotlin")

internal val Project.sessionsDir
    get() = basePersistentDir.projectSpecificCache(rootDir).resolve(SESSIONS_DIR_NAME)

internal val Project.metadataDir
    get() = basePersistentDir.projectSpecificCache(rootDir).resolve(METADATA_DIR_NAME)

internal val Project.errorsDir
    get() = basePersistentDir.projectSpecificCache(rootDir).resolve(ERRORS_DIR_NAME)

private val md5Digest by lazy { MessageDigest.getInstance("MD5") }

private fun File.absolutePathMd5Hash(): String = md5Digest.digest(absolutePath.toByteArray()).toHexString()

private fun File.projectSpecificCache(projectRootDir: File) = resolve(PROJECTS_CACHE_NAME_FULL).resolve(projectRootDir.absolutePathMd5Hash())
