/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.io

import kotlin.test.*
import java.io.File
import java.io.Reader
import java.io.StringReader
import java.net.URL
import java.nio.charset.Charset
import java.util.ArrayList

private fun sample(): Reader = StringReader("Hello\nWorld");

class ReadWriteTest {

    private fun createTempFileDeleteOnExit(): File =
        File.createTempFile("temp", System.nanoTime().toString()).also { it.deleteOnExit() }

    private fun Charset.encodeToByteArray(string: String): ByteArray =
        encode(string).let { it.array().copyOf(it.limit()) }

    private val hexFormat = HexFormat {
        bytes.bytesPerLine = 32
        bytes.bytesPerGroup = 8
    }

    private fun File.testContentEquals(expectedContent: ByteArray, charset: Charset) {
        val expected = expectedContent.toHexString(hexFormat)
        val actualContent = readBytes()
        val actual = actualContent.toHexString(hexFormat)
        assertEquals(expected, actual, "$charset. Expected size is ${expectedContent.size}, actual size is ${actualContent.size}")
    }

    private fun File.testWriteText(text: String, charset: Charset) {
        val encodedText = charset.encodeToByteArray(text)

        writeText(text, charset)
        testContentEquals(encodedText, charset)

        val prefix = "_"
        val encodedPrefix = charset.encodeToByteArray("_")

        writeText(prefix, charset)
        appendText(text, charset)
        testContentEquals(encodedPrefix + encodedText, charset)
    }

    @Test fun writeText() {
        val charsets = listOf(
            Charsets.UTF_8,
            Charsets.UTF_16,
            Charsets.UTF_32,
            Charsets.ISO_8859_1,
            Charsets.US_ASCII,
        )

        val highSurrogate = Char.MIN_HIGH_SURROGATE
        val lowSurrogate = Char.MIN_LOW_SURROGATE

        val smallString = "Hello"

        val chunkSize = DEFAULT_BUFFER_SIZE
        val string = "k".repeat(chunkSize - 1)

        val file = createTempFileDeleteOnExit()

        for (charset in charsets) {
            file.testWriteText("$highSurrogate", charset)

            file.testWriteText("$lowSurrogate", charset)

            file.testWriteText("$smallString$highSurrogate", charset)

            file.testWriteText("$smallString$lowSurrogate", charset)

            file.testWriteText("$string$highSurrogate", charset)

            file.testWriteText("$string$lowSurrogate", charset)

            file.testWriteText("$string$highSurrogate$lowSurrogate$string", charset)

            file.testWriteText("$string$lowSurrogate$highSurrogate$string", charset)

            file.testWriteText(
                "$string$highSurrogate$lowSurrogate${string.substring(2)}$highSurrogate$lowSurrogate",
                charset
            )

            file.testWriteText("$string$lowSurrogate$highSurrogate$lowSurrogate$string", charset)
        }
    }

    @Test fun testAppendText() {
        val file = createTempFileDeleteOnExit()
        file.writeText("Hello\n")
        file.appendText("World\n")
        file.appendText("Again")

        assertEquals("Hello\nWorld\nAgain", file.readText())
        assertEquals(listOf("Hello", "World", "Again"), file.readLines(Charsets.UTF_8))
    }

    @Test fun reader() {
        val list = ArrayList<String>()

        /* TODO would be nicer maybe to write this as
            reader.lines.forEach { ... }

          as we could one day maybe write that as
            for (line in reader.lines)

          if the for(elem in thing) {...} statement could act as syntax sugar for
            thing.forEach{ elem -> ... }

          if thing is not an Iterable/array/Iterator but has a suitable forEach method
        */
        sample().forEachLine {
            list.add(it)
        }
        assertEquals(listOf("Hello", "World"), list)

        assertEquals(listOf("Hello", "World"), sample().readLines())

        sample().useLines {
            assertEquals(listOf("Hello", "World"), it.toList())
        }


        var reader = StringReader("")
        var c = 0
        reader.forEachLine { c++ }
        assertEquals(0, c)

        reader = StringReader(" ")
        reader.forEachLine { c++ }
        assertEquals(1, c)

        reader = StringReader(" \n")
        c = 0
        reader.forEachLine { c++ }
        assertEquals(1, c)

        reader = StringReader(" \n ")
        c = 0
        reader.forEachLine { c++ }
        assertEquals(2, c)
    }

    @Test fun file() {
        val file = createTempFileDeleteOnExit()
        val writer = file.outputStream().writer().buffered()

        writer.write("Hello")
        writer.newLine()
        writer.write("World")
        writer.close()

        //file.replaceText("Hello\nWorld")
        file.forEachBlock { arr: ByteArray, size: Int ->
            assertTrue(size >= 11 && size <= 12, size.toString())
            assertTrue(arr.contains('W'.code.toByte()))
        }
        val list = ArrayList<String>()
        file.forEachLine(Charsets.UTF_8, {
            list.add(it)
        })
        assertEquals(arrayListOf("Hello", "World"), list)

        assertEquals(arrayListOf("Hello", "World"), file.readLines())

        file.useLines {
            assertEquals(arrayListOf("Hello", "World"), it.toList())
        }

        val text = file.inputStream().reader().readText()
        assertTrue(text.contains("Hello"))
        assertTrue(text.contains("World"))

        file.writeText("")
        var c = 0
        file.forEachLine { c++ }
        assertEquals(0, c)

        file.writeText(" ")
        file.forEachLine { c++ }
        assertEquals(1, c)

        file.writeText(" \n")
        c = 0
        file.forEachLine { c++ }
        assertEquals(1, c)

        file.writeText(" \n ")
        c = 0
        file.forEachLine { c++ }
        assertEquals(2, c)
    }

    @Test fun testURL() {
        val url = URL("http://kotlinlang.org")
        val text = url.readText()
        assertFalse(text.isEmpty())
        val text2 = url.readText(charset("UTF8"))
        assertFalse(text2.isEmpty())
    }
}


class LineIteratorTest {
    @Test fun useLines() {
        // TODO we should maybe zap the useLines approach as it encourages
        // use of iterators which don't close the underlying stream
        val list1 = sample().useLines { it.toList() }
        val list2 = sample().useLines<ArrayList<String>>{ it.toCollection(arrayListOf()) }

        assertEquals(listOf("Hello", "World"), list1)
        assertEquals(listOf("Hello", "World"), list2)
    }

    @Test fun manualClose() {
        val reader = sample().buffered()
        try {
            val list = reader.lineSequence().toList()
            assertEquals(arrayListOf("Hello", "World"), list)
        } finally {
            reader.close()
        }
    }

    @Test fun boundaryConditions() {
        var reader = StringReader("").buffered()
        assertEquals(emptyList(), reader.lineSequence().toList())
        reader.close()

        reader = StringReader(" ").buffered()
        assertEquals(listOf(" "), reader.lineSequence().toList())
        reader.close()

        reader = StringReader(" \n").buffered()
        assertEquals(listOf(" "), reader.lineSequence().toList())
        reader.close()

        reader = StringReader(" \n ").buffered()
        assertEquals(listOf(" ", " "), reader.lineSequence().toList())
        reader.close()
    }
}
