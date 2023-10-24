/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.utils

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider.Companion.kotlinPropertiesProvider
import java.io.File

private const val SESSIONS_DIR_NAME = "sessions"
private const val METADATA_DIR_NAME = "metadata"
private const val ERRORS_DIR_NAME = "errors"

@Suppress("unused") // will be used in the followup KT-58223 issues
internal val Project.userPersistentDir
    get() = kotlinPropertiesProvider.kotlinUserHomeDir?.let { File(it) }
        ?: File(System.getProperty("user.home")).resolve(".kotlin")

internal val Project.projectPersistentDir
    get() = kotlinPropertiesProvider.kotlinProjectPersistentDir?.let { File(it) }
        ?: rootDir.resolve(".kotlin")

internal val Project.kotlinSessionsDir
    get() = projectPersistentDir.resolve(SESSIONS_DIR_NAME)

internal val Project.kotlinMetadataDir
    get() = projectPersistentDir.resolve(METADATA_DIR_NAME)

internal val Project.kotlinErrorsDir
    get() = projectPersistentDir.resolve(ERRORS_DIR_NAME)
