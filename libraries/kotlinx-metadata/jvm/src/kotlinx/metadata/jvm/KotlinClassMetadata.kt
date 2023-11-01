/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
@file:Suppress(
    "DEPRECATION_ERROR", // Deprecated .accept implementation
    "UNUSED_PARAMETER" // For deprecated Writer.write
)

package kotlinx.metadata.jvm

import kotlinx.metadata.*
import kotlinx.metadata.internal.*
import kotlinx.metadata.jvm.internal.*
import org.jetbrains.kotlin.metadata.jvm.deserialization.JvmMetadataVersion
import org.jetbrains.kotlin.metadata.jvm.deserialization.JvmProtoBufUtil
import org.jetbrains.kotlin.metadata.jvm.serialization.JvmStringTable
import java.util.*

private object ReadHelpers {
    internal fun readKmClass(annotationData: Metadata): KmClass {
        val (strings, proto) = JvmProtoBufUtil.readClassDataFrom(annotationData.requireNotEmpty(), annotationData.data2)
        return proto.toKmClass(strings)
    }

    internal fun readKmPackage(annotationData: Metadata): KmPackage {
        val (strings, proto) = JvmProtoBufUtil.readPackageDataFrom(annotationData.requireNotEmpty(), annotationData.data2)
        return proto.toKmPackage(strings)
    }

    internal fun readKmLambda(annotationData: Metadata): KmLambda? {
        val functionData =
            annotationData.data1.takeIf(Array<*>::isNotEmpty)?.let { data1 ->
                JvmProtoBufUtil.readFunctionDataFrom(data1, annotationData.data2)
            } ?: return null
        val (strings, proto) = functionData
        return proto.toKmLambda(strings)
    }
}

/**
 * Represents the parsed metadata of a Kotlin JVM class file. Entry point for parsing metadata on JVM.
 *
 * To create an instance of [KotlinClassMetadata], first obtain an instance of [Metadata] annotation on a class file, and then call [KotlinClassMetadata.read].
 * [Metadata] annotation can be obtained either via reflection or created from data from a binary class file, using its constructor or helper function [kotlinx.metadata.jvm.Metadata].
 *
 * [KotlinClassMetadata] itself does not have any meaningful methods or properties; to work with it, it is required to do a `when` over subclasses.
 * Different subclasses of [KotlinClassMetadata] represent different kinds of class files produced by Kotlin compiler.
 * It is recommended to study documentation for each subclass and decide what subclasses one has to handle in the `when` expression,
 * trying to cover as much as possible.
 * Normally, one would need at least a [Class] and a [FileFacade], as these are two most common kinds.
 *
 * Most of the subclasses declare a property to view metadata as a Km data structure — for example, [KotlinClassMetadata.Class.kmClass].
 * Some of them also can contain additional properties, e.g. [KotlinClassMetadata.MultiFileClassPart.facadeClassName].
 * Km data structures represent Kotlin declarations and offer a variety of properties to introspect and alter them.
 * After desired changes are made, it is possible to get a new raw metadata instance with a corresponding `write` function, such as [KotlinClassMetadata.writeClass].
 *
 * Here is an example of reading a content name of some metadata:
 * ```
 * fun displayName(metadata: Metadata): String = when (val kcm = KotlinClassMetadata.read(metadata)) {
 *     is KotlinClassMetadata.Class -> "Class ${kcm.kmClass.name}"
 *     is KotlinClassMetadata.FileFacade -> "File facade with functions: ${kcm.kmPackage.functions.joinToString { it.name }}"
 *     is KotlinClassMetadata.SyntheticClass -> kcm.kmLambda?.function?.name?.let { "Lambda $it" } ?: "Synthetic class"
 *     is KotlinClassMetadata.MultiFileClassFacade -> "Multifile class facade with parts: ${kcm.partClassNames.joinToString()}"
 *     is KotlinClassMetadata.MultiFileClassPart -> "Multifile class part ${kcm.facadeClassName}"
 *     is KotlinClassMetadata.Unknown -> "Unknown metadata"
 * }
 * ```
 */
public sealed class KotlinClassMetadata() {

    /**
     * Represents metadata of a class file containing a declaration of a Kotlin class.
     *
     * Anything that does not belong to a Kotlin class (top-level declarations) is not present in
     * [Class] metadata, even if such declaration was in the same source file. See [FileFacade] for details.
     */
    public class Class public constructor(public var kmClass: KmClass, public val version: IntArray, public val flags: Int) :
        KotlinClassMetadata() {

        internal constructor(annotationData: Metadata, lenient: Boolean) : this(
            ReadHelpers.readKmClass(annotationData).also { if (lenient) KmInternalAccessors.disallowWrite(it) },
            annotationData.metadataVersion,
            annotationData.extraInt
        )

        /**
         * Returns a new [KmClass] instance created from this class metadata.
         */
        @Deprecated(
            "To avoid excessive copying, use .kmClass property instead. Note that it returns a view and not a copy.",
            ReplaceWith("kmClass"),
            DeprecationLevel.WARNING
        )
        public fun toKmClass(): KmClass = KmClass().also { newKm -> kmClass.accept(newKm) } // defensive copy

        /**
         * Makes the given visitor visit the metadata of this class.
         *
         * @param v the visitor that must visit this class
         */
        @Deprecated(VISITOR_API_MESSAGE, level = DeprecationLevel.ERROR)
        public fun accept(v: KmClassVisitor): Unit = kmClass.accept(v)

        /**
         * A [KmClassVisitor] that generates the metadata of a Kotlin class.
         */
        @Deprecated(
            "$WRITER_API_MESSAGE, such as KotlinClassMetadata.writeClass(kmClass, metadataVersion, extraInt)",
            level = DeprecationLevel.ERROR
        )
        public class Writer : ClassWriter(JvmStringTable()) {
            /**
             * Returns the metadata of the class that was written with this writer.
             *
             * @param metadataVersion metadata version to be written to the metadata (see [Metadata.metadataVersion]),
             *   [KotlinClassMetadata.COMPATIBLE_METADATA_VERSION] by default. Cannot be less (lexicographically) than `[1, 4]`
             * @param extraInt the value of the class-level flags to be written to the metadata (see [Metadata.extraInt]),
             *   0 by default
             */
            @JvmOverloads
            @Deprecated(
                "$WRITER_API_MESSAGE, such as KotlinClassMetadata.writeClass(kmClass, metadataVersion, extraInt)",
                level = DeprecationLevel.ERROR
            )
            public fun write(
                metadataVersion: IntArray = COMPATIBLE_METADATA_VERSION,
                extraInt: Int = 0
            ): Class {
                error("This method is no longer implemented. Migrate to KotlinClassMetadata.writeClass.")
            }
        }
    }

    /**
     * Represents metadata of a class file containing a compiled Kotlin file facade.
     *
     * File facade is a JVM class that contains declarations which do not belong to any Kotlin class: top-level functions, properties, and type aliases.
     * For example, file Main.kt that contains only `fun main()` would produce a `MainKt.class` with FileFacade with this function metadata.
     * If Kotlin source file contains both classes and top-level declarations, only top-level declarations would be available in the corresponding file facade.
     * Classes would have their own JVM classfiles and their own metadata of [Class] kind.
     */
    public class FileFacade public constructor(public var kmPackage: KmPackage, public val version: IntArray, public val flags: Int) :
        KotlinClassMetadata() {

        internal constructor(annotationData: Metadata, lenient: Boolean) : this(
            ReadHelpers.readKmPackage(annotationData).also { if (lenient) KmInternalAccessors.disallowWrite(it) },
            annotationData.metadataVersion,
            annotationData.extraInt
        )

        /**
         * Returns the [KmPackage] representation of this metadata.
         *
         * Returns the same (mutable) [KmPackage] instance every time.
         */
//        public val kmPackage: KmPackage


        /**
         * Creates a new [KmPackage] instance from this file facade metadata.
         */
        @Deprecated(
            "To avoid excessive copying, use .kmPackage property instead. Note that it returns a view and not a copy.",
            ReplaceWith("kmPackage"),
            DeprecationLevel.WARNING
        )
        public fun toKmPackage(): KmPackage = KmPackage().also { newPkg -> kmPackage.accept(newPkg) }

        /**
         * Makes the given visitor visit metadata of this file facade.
         *
         * @param v the visitor that must visit this file facade
         */
        @Deprecated(VISITOR_API_MESSAGE, level = DeprecationLevel.ERROR)
        public fun accept(v: KmPackageVisitor): Unit = kmPackage.accept(v)

        /**
         * A [KmPackageVisitor] that generates the metadata of a Kotlin file facade.
         */
        @Deprecated(
            "$WRITER_API_MESSAGE, such as KotlinClassMetadata.writeFileFacade(kmPackage, metadataVersion, extraInt)",
            level = DeprecationLevel.ERROR
        )
        public class Writer : PackageWriter(JvmStringTable()) {
            /**
             * Returns the metadata of the file facade that was written with this writer.
             *
             * @param metadataVersion metadata version to be written to the metadata (see [Metadata.metadataVersion]),
             *   [KotlinClassMetadata.COMPATIBLE_METADATA_VERSION] by default. Cannot be less (lexicographically) than `[1, 4]`
             * @param extraInt the value of the class-level flags to be written to the metadata (see [Metadata.extraInt]),
             *   0 by default
             */
            @JvmOverloads
            @Deprecated(
                "$WRITER_API_MESSAGE, such as KotlinClassMetadata.writeFileFacade(kmPackage, metadataVersion, extraInt)",
                level = DeprecationLevel.ERROR
            )
            public fun write(
                metadataVersion: IntArray = COMPATIBLE_METADATA_VERSION,
                extraInt: Int = 0
            ): FileFacade {
                error("This method is no longer implemented. Migrate to KotlinClassMetadata.writeFileFacade.")
            }
        }
    }

    /**
     * Represents metadata of a class file containing a synthetic class, e.g. a class for lambda, `$DefaultImpls` class for interface
     * method implementations, `$WhenMappings` class for optimized `when` over enums, etc.
     */
    public class SyntheticClass(public var kmLambda: KmLambda?, public val version: IntArray, public val flags: Int) :
        KotlinClassMetadata() {

        internal var allowedToWrite: Boolean = true

        internal constructor(annotationData: Metadata, lenient: Boolean) : this(
            ReadHelpers.readKmLambda(annotationData)?.also { if (lenient) KmInternalAccessors.disallowWrite(it) },
            annotationData.metadataVersion,
            annotationData.extraInt
        ) {
            if (lenient) allowedToWrite = false
        }

        /**
         * Returns `true` if this synthetic class is a class file compiled for a Kotlin lambda.
         */
        public val isLambda: Boolean
            get() = kmLambda != null


        /**
         * Returns the [KmLambda] representation of this metadata, or `null` if this synthetic class does not represent a lambda.
         *
         * Returns the same (mutable) [KmLambda] instance every time.
         */
//        public val kmLambda: KmLambda?


        /**
         * Creates a new [KmLambda] instance from this synthetic class metadata.
         * Returns `null` if this synthetic class does not represent a lambda.
         */
        @Deprecated(
            "To avoid excessive copying, use .kmLambda property instead. Note that it returns a view and not a copy.",
            ReplaceWith("kmLambda"),
            DeprecationLevel.WARNING
        )
        public fun toKmLambda(): KmLambda? = if (isLambda) KmLambda().apply(this::accept) else null

        /**
         * Makes the given visitor visit metadata of this file facade if this synthetic class represents a Kotlin lambda
         * (`isLambda` == true).
         *
         * Throws [IllegalArgumentException] if this synthetic class does not represent a Kotlin lambda.
         *
         * @param v the visitor that must visit this lambda
         */
        @Deprecated(VISITOR_API_MESSAGE, level = DeprecationLevel.ERROR)
        public fun accept(v: KmLambdaVisitor) {
            if (!isLambda) throw IllegalArgumentException(
                "accept(KmLambdaVisitor) is only possible for synthetic classes which are lambdas (isLambda = true)"
            )

            kmLambda!!.accept(v)
        }

        /**
         * A [KmLambdaVisitor] that generates the metadata of a synthetic class. To generate metadata of a Kotlin lambda,
         * call [Writer.visitFunction] and [Writer.visitEnd] on a newly created instance of this writer. If these methods are not called,
         * the resulting metadata will represent a _non-lambda_ synthetic class.
         */
        @Deprecated(
            WRITER_API_MESSAGE + ": KotlinClassMetadata.writeLambda(kmLambda, metadataVersion, extraInt) " +
                    "or KotlinClassMetadata.writeSyntheticClass(metadataVersion, extraInt) for a non-lambda synthetic class",
            level = DeprecationLevel.ERROR
        )
        public class Writer : LambdaWriter(JvmStringTable()) {
            /**
             * Returns the metadata of the synthetic class that was written with this writer.
             *
             * @param metadataVersion metadata version to be written to the metadata (see [Metadata.metadataVersion]),
             *   [KotlinClassMetadata.COMPATIBLE_METADATA_VERSION] by default. Cannot be less (lexicographically) than `[1, 4]`
             * @param extraInt the value of the class-level flags to be written to the metadata (see [Metadata.extraInt]),
             *   0 by default
             */
            @Deprecated(
                WRITER_API_MESSAGE + ": KotlinClassMetadata.writeLambda(kmLambda, metadataVersion, extraInt) " +
                        "or KotlinClassMetadata.writeSyntheticClass(metadataVersion, extraInt) for a non-lambda synthetic class",
                level = DeprecationLevel.ERROR
            )
            @JvmOverloads
            public fun write(
                metadataVersion: IntArray = COMPATIBLE_METADATA_VERSION,
                extraInt: Int = 0
            ): SyntheticClass {
                error("This method is no longer implemented. Migrate to KotlinClassMetadata.writeLambda or KotlinClassMetadata.writeSyntheticClass.")
            }
        }
    }

    /**
     * Represents metadata of a class file containing a compiled multi-file class facade.
     *
     * Multi-file class facade is a facade file produced from several Kotlin source files marked with [JvmMultifileClass] and same [JvmName].
     * It does not have any declarations; it only contains [partClassNames] to indicate where individual parts are located.
     *
     * Consider the following example.
     * Suppose we have two files, partOne.kt and partTwo.kt:
     *
     * ```
     * // partOne.kt
     * @file:JvmMultifileClass
     * @file:JvmName("MultiFileClass")
     *
     * fun partOne(): String = "one"
     *
     * // partTwo.kt
     * @file:JvmMultifileClass
     * @file:JvmName("MultiFileClass")
     *
     * fun partTwo(): String = "two"
     * ```
     *
     * In this case, there would be three classfiles produced by the compiler, each with its own metadata.
     * Metadata for `MultiFileClass.class` would be of type [MultiFileClassFacade]
     * and contain [partClassNames] that would indicate class file names for the parts: `[MultiFileClass__PartOneKt, MultiFileClass__PartTwoKt]`.
     * Using these names, you can load metadata from those classes with type [MultiFileClassPart].
     *
     * @see MultiFileClassPart
     * @see JvmMultifileClass
     */
    public class MultiFileClassFacade(public var partClassNames: List<String>, public val version: IntArray, public val flags: Int) :
        KotlinClassMetadata() {

        internal var allowedToWrite: Boolean = true

        internal constructor(annotationData: Metadata, lenient: Boolean) : this(
            annotationData.data1.asList(),
            annotationData.metadataVersion,
            annotationData.extraInt
        ) {
            if (lenient) allowedToWrite = false
        }
        /**
         * JVM internal names of the part classes which this multi-file class combines.
         */
//        public val partClassNames: List<String> = annotationData.data1.asList()

        /**
         * A writer that generates the metadata of a multi-file class facade.
         */
        @Deprecated(
            "$WRITER_API_MESSAGE, such as KotlinClassMetadata.writeMultiFileClassFacade(partClassNames, metadataVersion, extraInt)",
            level = DeprecationLevel.ERROR
        )
        public class Writer {
            /**
             * Returns the metadata of the multi-file class facade that was written with this writer.
             *
             * @param partClassNames JVM internal names of the part classes which this multi-file class combines
             * @param metadataVersion metadata version to be written to the metadata (see [Metadata.metadataVersion]),
             *   [KotlinClassMetadata.COMPATIBLE_METADATA_VERSION] by default. Cannot be less (lexicographically) than `[1, 4]`
             * @param extraInt the value of the class-level flags to be written to the metadata (see [Metadata.extraInt]),
             *   0 by default
             */
            @Deprecated(
                "$WRITER_API_MESSAGE, such as KotlinClassMetadata.writeMultiFileClassFacade(partClassNames, metadataVersion, extraInt)",
                level = DeprecationLevel.ERROR
            )
            @JvmOverloads
            public fun write(
                partClassNames: List<String>,
                metadataVersion: IntArray = COMPATIBLE_METADATA_VERSION,
                extraInt: Int = 0
            ): MultiFileClassFacade {
                error("This method is no longer implemented. Migrate to KotlinClassMetadata.writeMultiFileClassFacade.")
            }
        }
    }

    /**
     * Represents metadata of a class file containing a compiled multi-file class part, i.e. an internal class with method bodies
     * and their metadata, accessed only from the corresponding [facade][MultiFileClassFacade]. Just like [FileFacade], this metadata contains only top-level declarations,
     * as classes have their own one.
     *
     * It does not contain any references to other parts; to locate all the parts, one should construct a corresponding
     * [facade][MultiFileClassFacade] beforehand.
     *
     * @see MultiFileClassFacade
     * @see JvmMultifileClass
     */
    public class MultiFileClassPart public constructor(
        public val kmPackage: KmPackage,
        public val facadeClassName: String,
        public val version: IntArray,
        public val flags: Int,
    ) : KotlinClassMetadata() {

        internal constructor(annotationData: Metadata, lenient: Boolean) : this(
            ReadHelpers.readKmPackage(annotationData).also { if (lenient) KmInternalAccessors.disallowWrite(it) },
            annotationData.extraString,
            annotationData.metadataVersion,
            annotationData.extraInt
        )


        /**
         * Returns the [KmPackage] representation of this metadata.
         *
         * Returns the same (mutable) [KmPackage] instance every time.
         */
//        public val kmPackage: KmPackage


        /**
         * JVM internal name of the corresponding multi-file class facade.
         */
//        public val facadeClassName: String
//            get() = annotationData.extraString

        /**
         * Creates a new [KmPackage] instance from this multi-file class part metadata.
         */
        @Deprecated(
            "To avoid excessive copying, use .kmPackage property instead. Note that it returns a view and not a copy.",
            ReplaceWith("kmPackage"),
            DeprecationLevel.WARNING
        )
        public fun toKmPackage(): KmPackage = KmPackage().also { newKmp -> kmPackage.accept(newKmp) }

        /**
         * Makes the given visitor visit metadata of this multi-file class part.
         *
         * @param v the visitor that must visit this multi-file class part
         */
        @Deprecated(VISITOR_API_MESSAGE, level = DeprecationLevel.ERROR)
        public fun accept(v: KmPackageVisitor) {
            kmPackage.accept(v)
        }

        /**
         * A [KmPackageVisitor] that generates the metadata of a multi-file class part.
         */
        @Deprecated(
            "$WRITER_API_MESSAGE, such as KotlinClassMetadata.writeMultiFileClassPart(kmPackage, facadeClassName, metadataVersion, extraInt)",
            level = DeprecationLevel.ERROR
        )
        public class Writer : PackageWriter(JvmStringTable()) {
            /**
             * Returns the metadata of the multi-file class part that was written with this writer.
             *
             * @param facadeClassName JVM internal name of the corresponding multi-file class facade
             * @param metadataVersion metadata version to be written to the metadata (see [Metadata.metadataVersion]),
             *   [KotlinClassMetadata.COMPATIBLE_METADATA_VERSION] by default. Cannot be less (lexicographically) than `[1, 4]`
             * @param extraInt the value of the class-level flags to be written to the metadata (see [Metadata.extraInt]),
             *   0 by default
             */
            @Deprecated(
                "$WRITER_API_MESSAGE, such as KotlinClassMetadata.writeMultiFileClassPart(kmPackage, facadeClassName, metadataVersion, extraInt)",
                level = DeprecationLevel.ERROR
            )
            @JvmOverloads
            public fun write(
                facadeClassName: String,
                metadataVersion: IntArray = COMPATIBLE_METADATA_VERSION,
                extraInt: Int = 0
            ): MultiFileClassPart {
                error("This method is no longer implemented. Migrate to KotlinClassMetadata.writeMultifileClassPart.")
            }
        }
    }

    /**
     * Represents metadata of an unknown class file. This class is used if an old version of this library is used against a new kind
     * of class files generated by the Kotlin compiler, unsupported by this library.
     */
    public data object Unknown : KotlinClassMetadata()

    /**
     * Collection of methods for reading and writing [KotlinClassMetadata],
     * as well as metadata kind constants and [COMPATIBLE_METADATA_VERSION] constant.
     */
    public companion object {

        public fun transform(metadata: Metadata, transformer: (KotlinClassMetadata) -> Unit): Metadata {
            val original = readStrict(metadata)
            transformer(original)
            return write(original)
        }

        public fun write(metadata: KotlinClassMetadata): Metadata {
            return when(metadata) {
                is Class -> writeClass(metadata.kmClass, metadata.version, metadata.flags)
                is FileFacade -> writeFileFacade(metadata.kmPackage, metadata.version, metadata.flags)
                is SyntheticClass -> {
                    throwIfNotWriteable(metadata.allowedToWrite, "synthetic class")
                    if (metadata.isLambda) writeLambda(metadata.kmLambda!!, metadata.version, metadata.flags) else writeSyntheticClass(metadata.version, metadata.flags)
                }
                is MultiFileClassFacade -> {
                    throwIfNotWriteable(metadata.allowedToWrite, "multi file class facade")
                    writeMultiFileClassFacade(metadata.partClassNames, metadata.version, metadata.flags)
                }
                is MultiFileClassPart -> writeMultiFileClassPart(metadata.kmPackage, metadata.facadeClassName, metadata.version, metadata.flags)
                is Unknown -> throw IllegalArgumentException("Can't write unknown metadata")
            }
        }


        /**
         * Writes contents of [kmClass] as the class metadata.
         *
         * @param metadataVersion metadata version to be written to the metadata (see [Metadata.metadataVersion]),
         *   [KotlinClassMetadata.COMPATIBLE_METADATA_VERSION] by default. Cannot be less (lexicographically) than `[1, 4]`
         * @param extraInt the value of the class-level flags to be written to the metadata (see [Metadata.extraInt]),
         *   0 by default
         *
         * @throws IllegalArgumentException if [kmClass] is not correct and cannot be written or if [metadataVersion] is not supported for writing.
         */
        @JvmStatic
        @JvmOverloads
        public fun writeClass(
            kmClass: KmClass,
            metadataVersion: IntArray = COMPATIBLE_METADATA_VERSION,
            extraInt: Int = 0,
        ): Metadata = wrapWriteIntoIAE {
            checkMetadataVersion(metadataVersion)
            throwIfNotWriteable(KmInternalAccessors.isWriteable(kmClass), "class")
            val writer = ClassWriter(JvmStringTable())
            writer.writeClass(kmClass)
            val (d1, d2) = writeProtoBufData(writer.t.build(), writer.c)
            Metadata(CLASS_KIND, metadataVersion, d1, d2, extraInt = extraInt)
        }


        /**
         * Writes [kmPackage] contents as the file facade metadata.
         *
         * @param metadataVersion metadata version to be written to the metadata (see [Metadata.metadataVersion]),
         *   [KotlinClassMetadata.COMPATIBLE_METADATA_VERSION] by default. Cannot be less (lexicographically) than `[1, 4]`
         * @param extraInt the value of the class-level flags to be written to the metadata (see [Metadata.extraInt]),
         *   0 by default
         *
         * @throws IllegalArgumentException if [kmPackage] is not correct and cannot be written or if [metadataVersion] is not supported for writing.
         */
        @JvmStatic
        @JvmOverloads
        public fun writeFileFacade(
            kmPackage: KmPackage,
            metadataVersion: IntArray = COMPATIBLE_METADATA_VERSION,
            extraInt: Int = 0,
        ): Metadata = wrapWriteIntoIAE {
            checkMetadataVersion(metadataVersion)
            throwIfNotWriteable(KmInternalAccessors.isWriteable(kmPackage), "file facade")
            val writer = PackageWriter(JvmStringTable())
            writer.writePackage(kmPackage)
            val (d1, d2) = writeProtoBufData(writer.t.build(), writer.c)
            Metadata(FILE_FACADE_KIND, metadataVersion, d1, d2, extraInt = extraInt)
        }

        /**
         * Writes [kmLambda] as the synthetic class metadata.
         *
         * @param metadataVersion metadata version to be written to the metadata (see [Metadata.metadataVersion]),
         *   [KotlinClassMetadata.COMPATIBLE_METADATA_VERSION] by default. Cannot be less (lexicographically) than `[1, 4]`
         * @param extraInt the value of the class-level flags to be written to the metadata (see [Metadata.extraInt]),
         *   0 by default
         *
         * @throws IllegalArgumentException if [kmLambda] is not correct and cannot be written or if [metadataVersion] is not supported for writing.
         */
        @JvmStatic
        @JvmOverloads
        public fun writeLambda(
            kmLambda: KmLambda,
            metadataVersion: IntArray = COMPATIBLE_METADATA_VERSION,
            extraInt: Int = 0,
        ): Metadata = wrapWriteIntoIAE {
            checkMetadataVersion(metadataVersion)
            throwIfNotWriteable(KmInternalAccessors.isWriteable(kmLambda), "lambda")
            val writer = LambdaWriter(JvmStringTable())
            writer.writeLambda(kmLambda)
            val proto = writer.t?.build()
            val (d1, d2) =
                if (proto != null) writeProtoBufData(proto, writer.c)
                else Pair(emptyArray(), emptyArray())
            Metadata(SYNTHETIC_CLASS_KIND, metadataVersion, d1, d2, extraInt = extraInt)
        }

        /**
         * Writes synthetic class metadata.
         *
         * @param metadataVersion metadata version to be written to the metadata (see [Metadata.metadataVersion]),
         *   [KotlinClassMetadata.COMPATIBLE_METADATA_VERSION] by default. Cannot be less (lexicographically) than `[1, 4]`
         * @param extraInt the value of the class-level flags to be written to the metadata (see [Metadata.extraInt]),
         *   0 by default
         *
         * @throws IllegalArgumentException if [metadataVersion] is not supported for writing.
         */
        @JvmStatic
        @JvmOverloads
        public fun writeSyntheticClass(
            metadataVersion: IntArray = COMPATIBLE_METADATA_VERSION,
            extraInt: Int = 0,
        ): Metadata {
            checkMetadataVersion(metadataVersion)
            return Metadata(SYNTHETIC_CLASS_KIND, metadataVersion, emptyArray(), emptyArray(), extraInt = extraInt)
        }

        /**
         * Writes metadata of the multi-file class facade.
         *
         * @param partClassNames JVM internal names of the part classes which this multi-file class combines
         * @param metadataVersion metadata version to be written to the metadata (see [Metadata.metadataVersion]),
         *   [KotlinClassMetadata.COMPATIBLE_METADATA_VERSION] by default. Cannot be less (lexicographically) than `[1, 4]`
         * @param extraInt the value of the class-level flags to be written to the metadata (see [Metadata.extraInt]),
         *   0 by default
         *
         * @throws IllegalArgumentException if [metadataVersion] is not supported for writing.
         */
        @JvmStatic
        @JvmOverloads
        public fun writeMultiFileClassFacade(
            partClassNames: List<String>, metadataVersion: IntArray = COMPATIBLE_METADATA_VERSION,
            extraInt: Int = 0,
        ): Metadata {
            checkMetadataVersion(metadataVersion)
            return Metadata(
                MULTI_FILE_CLASS_FACADE_KIND, metadataVersion,
                partClassNames.toTypedArray(), extraInt = extraInt
            )
        }

        /**
         * Writes the metadata of the multi-file class part.
         *
         * @param facadeClassName JVM internal name of the corresponding multi-file class facade
         * @param metadataVersion metadata version to be written to the metadata (see [Metadata.metadataVersion]),
         *   [KotlinClassMetadata.COMPATIBLE_METADATA_VERSION] by default. Cannot be less (lexicographically) than `[1, 4]`
         * @param extraInt the value of the class-level flags to be written to the metadata (see [Metadata.extraInt]),
         *   0 by default
         *
         * @throws IllegalArgumentException if [kmPackage] is not correct and cannot be written or if [metadataVersion] is not supported for writing.
         */
        @JvmStatic
        @JvmOverloads
        public fun writeMultiFileClassPart(
            kmPackage: KmPackage,
            facadeClassName: String,
            metadataVersion: IntArray = COMPATIBLE_METADATA_VERSION,
            extraInt: Int = 0,
        ): Metadata = wrapWriteIntoIAE {
            checkMetadataVersion(metadataVersion)
            throwIfNotWriteable(KmInternalAccessors.isWriteable(kmPackage), "multi-file class part")
            val writer = PackageWriter(JvmStringTable())
            writer.writePackage(kmPackage)
            val (d1, d2) = writeProtoBufData(writer.t.build(), writer.c)
            Metadata(
                MULTI_FILE_CLASS_PART_KIND, metadataVersion, d1, d2, facadeClassName, extraInt = extraInt
            )
        }

        /**
         * Reads and parses the given annotation data of a Kotlin JVM class file and returns the correct type of [KotlinClassMetadata] encoded by
         * this annotation, if metadata version is supported.
         *
         * [annotationData] may be obtained reflectively, constructed manually or with helper [kotlinx.metadata.jvm.Metadata] function,
         * or equivalent [KotlinClassHeader] can be used.
         *
         * Metadata version is supported if it is greater or equal than 1.1, and less or equal than [COMPATIBLE_METADATA_VERSION] + 1 minor version.
         * Note that metadata version is 1.1 for Kotlin < 1.4, and is equal to the language version starting from Kotlin 1.4.
         * For example, if the latest Kotlin version is 1.7.0, the latest kotlinx-metadata-jvm can read binaries produced by Kotlin
         * compilers from 1.0 to 1.8.* inclusively.
         *
         * @throws IllegalArgumentException if the metadata version is unsupported
         *
         * @see COMPATIBLE_METADATA_VERSION
         */
        @JvmStatic
        @Deprecated("todo", ReplaceWith("KotlinClassMetadata.readStrict(annotationData)"), DeprecationLevel.WARNING)
        public fun read(annotationData: Metadata): KotlinClassMetadata = readImpl(annotationData, lenient = false)

        @JvmStatic
        public fun readStrict(annotationData: Metadata): KotlinClassMetadata = readImpl(annotationData, lenient = false)

        @JvmStatic
        public fun readLenient(annotationData: Metadata): KotlinClassMetadata = readImpl(annotationData, lenient = true)

        private fun readImpl(annotationData: Metadata, lenient: Boolean): KotlinClassMetadata {
            checkMetadataVersionForRead(annotationData, lenient)

            return wrapIntoMetadataExceptionWhenNeeded {
                when (annotationData.kind) {
                    CLASS_KIND -> Class(annotationData, lenient)
                    FILE_FACADE_KIND -> FileFacade(annotationData, lenient)
                    SYNTHETIC_CLASS_KIND -> SyntheticClass(annotationData, lenient)
                    MULTI_FILE_CLASS_FACADE_KIND -> MultiFileClassFacade(annotationData, lenient)
                    MULTI_FILE_CLASS_PART_KIND -> MultiFileClassPart(annotationData, lenient)
                    else -> Unknown
                }
            }
        }

        private fun checkMetadataVersionForRead(annotationData: Metadata, lenient: Boolean) {
            if (annotationData.metadataVersion.isEmpty())
                throw IllegalArgumentException("Provided Metadata instance does not have metadataVersion in it and therefore is malformed and cannot be read.")
            val jvmMetadataVersion = JvmMetadataVersion(
                annotationData.metadataVersion,
                (annotationData.extraInt and (1 shl 3)/* see JvmAnnotationNames.METADATA_STRICT_VERSION_SEMANTICS_FLAG */) != 0
            )
            throwIfNotCompatible(jvmMetadataVersion, lenient)
        }

        internal fun throwIfNotCompatible(jvmMetadataVersion: JvmMetadataVersion, lenient: Boolean) {
            val isAtLeast110 = jvmMetadataVersion.isAtLeast(1, 1, 0)
            val isCompatible = if (lenient) isAtLeast110 else jvmMetadataVersion.isCompatibleWithCurrentCompilerVersion()
            if (!isCompatible) {
                // Kotlin 1.0 produces classfiles with metadataVersion = 1.1.0, while 1.0.0 represents unsupported pre-1.0 Kotlin (see JvmMetadataVersion.kt:39)
                val postfix =
                    if (!isAtLeast110) "while minimum supported version is 1.1.0 (Kotlin 1.0)."
                    else "while maximum supported version is ${if (jvmMetadataVersion.isStrictSemantics) JvmMetadataVersion.INSTANCE else JvmMetadataVersion.INSTANCE_NEXT}. To support newer versions, update the kotlinx-metadata-jvm library."
                throw IllegalArgumentException("Provided Metadata instance has version $jvmMetadataVersion, $postfix")
            }
        }

        private fun checkMetadataVersion(version: IntArray) {
            require(version.size >= 2 && version[0] >= 1 && (version[0] > 1 || version[1] >= 4)) {
                "This version of kotlinx-metadata-jvm doesn't support writing Kotlin metadata of version earlier than 1.4. " +
                        "Please change the version from ${version.toList()} to at least [1, 4]."
            }
        }

        internal fun throwIfNotWriteable(writeable: Boolean, name: String) {
            if (writeable) return
            throw IllegalArgumentException("This $name cannot be written because it represents $name read in lenient mode")
        }

        /**
         * A class file kind signifying that the corresponding class file contains a declaration of a Kotlin class.
         *
         * @see Metadata.kind
         */
        public const val CLASS_KIND: Int = 1

        /**
         * A class file kind signifying that the corresponding class file is a compiled Kotlin file facade.
         *
         * @see Metadata.kind
         */
        public const val FILE_FACADE_KIND: Int = 2

        /**
         * A class file kind signifying that the corresponding class file is synthetic, e.g. it is a class for lambda, `$DefaultImpls` class
         * for interface method implementations, `$WhenMappings` class for optimized `when` over enums, etc.
         *
         * @see Metadata.kind
         */
        public const val SYNTHETIC_CLASS_KIND: Int = 3

        /**
         * A class file kind signifying that the corresponding class file is a compiled multi-file class facade.
         *
         * @see Metadata.kind
         *
         * @see JvmMultifileClass
         */
        public const val MULTI_FILE_CLASS_FACADE_KIND: Int = 4

        /**
         * A class file kind signifying that the corresponding class file is a compiled multi-file class part, i.e. an internal class
         * with method bodies and their metadata, accessed only from the corresponding facade.
         *
         * @see Metadata.kind
         *
         * @see JvmMultifileClass
         */
        public const val MULTI_FILE_CLASS_PART_KIND: Int = 5

        /**
         * The latest stable metadata version supported by this version of the library.
         * The library can read Kotlin metadata produced by Kotlin compilers from 1.0 up to and including this version + 1 minor.
         *
         * For example, if the latest supported stable Kotlin version is 1.7.0, kotlinx-metadata-jvm can read binaries produced by Kotlin compilers from 1.0
         * to 1.8.* inclusively. In this case, this property will have the value `[1, 7, 0]`.
         *
         * @see Metadata.metadataVersion
         */
        @JvmField  // TODO: move it somewhere since it is also used in KotlinModuleMetadata?
        public val COMPATIBLE_METADATA_VERSION: IntArray = JvmMetadataVersion.INSTANCE.toArray().copyOf()
    }
}

internal const val VISITOR_API_MESSAGE =
    "Visitor API is deprecated as excessive and cumbersome. Please use nodes (such as KmClass) and their properties."

internal const val WRITER_API_MESSAGE =
    "Visitor Writer API is deprecated as excessive and cumbersome. Please use member functions of KotlinClassMetadata.Companion"
