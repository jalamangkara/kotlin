/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.text

import kotlin.test.*

class StringNativeTest {

    @Test
    fun lowercase() {
        // Non-ASCII
        assertEquals("\u214E\uA7B5\u2CEF", "\u2132\uA7B4\u2CEF".lowercase())

        // Surrogate pairs
        assertEquals("\uD801\uDC4F\uD806\uDCD0\uD81B\uDE7F", "\uD801\uDC27\uD806\uDCB0\uD81B\uDE5F".lowercase())

        // Special Casing
        // LATIN CAPITAL LETTER I WITH DOT ABOVE
        assertEquals("\u0069\u0307", "\u0130".lowercase())
        assertEquals("a\u0069\u0307z", "a\u0130Z".lowercase())

        // Final_Sigma
        run {
            fun caseIgnorable(length: Int): String {
                return listOf(
                        "\u0483",       // COMBINING CYRILLIC TITLO; Mn
                        "\u20DF",       // COMBINING ENCLOSING DIAMOND; Me
                        "\uD804\uDCBD", // 110BD; KAITHI NUMBER SIGN; Cf
                        "\u2C7D",       // MODIFIER LETTER CAPITAL V; Lm
                        "\uD83C\uDFFD", // 1F3FD; EMOJI MODIFIER FITZPATRICK TYPE-4; Sk
                        "\u003A",       // COLON; Po; Word_Break=MidLetter
                        "\uFF0E",       // FULLWIDTH FULL STOP; Po; Word_Break=MidNumLet
                        "\u0027"        // APOSTROPHE; Po; Word_Break=Single_Quote
                ).shuffled().take(length).joinToString(separator = "")
            }

            fun cased(): String {
                return listOf(
                        "\u0041",       // LATIN CAPITAL LETTER A; Lu
                        "\uD81B\uDE63", // 16E63; MEDEFAIDRIN SMALL LETTER W; Ll
                        "\u01CB",       // LATIN CAPITAL LETTER N WITH SMALL LETTER J; Lt
                        "\u217A",       // SMALL ROMAN NUMERAL ELEVEN; Nl; Other_Lowercase
                        "\uD83C\uDD50"  // 1F150; NEGATIVE CIRCLED LATIN CAPITAL LETTER A; So; Other_Uppercase
                ).random()
            }

            fun other(): String {
                return listOf(
                        "\u0000",       // <control>; Cc
                        "\u0030",       // DIGIT ZERO; Nd
                        "\uD838\uDEC6"  // 1E2C6; WANCHO LETTER YA; Lo
                ).random()
            }

            fun Char.hex(): String {
                return code.toString(16).padStart(4, '0')
            }

            val sigma = '\u03A3'
            val lowerSigma = '\u03C3'
            val specialLowerSigma = '\u03C2'

            // Build a string of the form: [cased][other](caseIgnorable*)(sigma)(caseIgnorable*)[other][cased]
            for (precedingCaseIgnorable in 0..5) {
                for (succeedingCaseIgnorable in 0..5) {
                    val caseIgnorableBefore = caseIgnorable(precedingCaseIgnorable)
                    val caseIgnorableAfter = caseIgnorable(succeedingCaseIgnorable)
                    val sigmaNearby = caseIgnorableBefore + sigma + caseIgnorableAfter
                    val lowerSigmaNearby = caseIgnorableBefore + lowerSigma + caseIgnorableAfter
                    val specialSigmaNearby = caseIgnorableBefore + specialLowerSigma + caseIgnorableAfter

                    for (mask in 0 until (1 shl 4)) {
                        val casedBefore = cased().repeat((mask shr 0) and 1)
                        val casedAfter = cased().repeat((mask shr 1) and 1)
                        val otherBefore = other().repeat((mask shr 2) and 1)
                        val otherAfter = other().repeat((mask shr 3) and 1)

                        val resultSigmaNearby =
                                if (otherBefore.isEmpty() && casedBefore.isNotEmpty() && (otherAfter.isNotEmpty() || casedAfter.isEmpty()))
                                    specialSigmaNearby
                                else
                                    lowerSigmaNearby

                        val actual = (casedBefore + otherBefore + sigmaNearby + otherAfter + casedAfter).lowercase()
                        val expected = casedBefore.lowercase() + otherBefore + resultSigmaNearby + otherAfter + casedAfter.lowercase()

                        assertEquals(
                                expected,
                                actual,
                                "Expected <$expected>${expected.map { it.hex() }}, Actual <$actual>${actual.map { it.hex() }}"
                        )
                    }
                }
            }
        }
    }

    @Test
    fun uppercase() {
        // Non-ASCII
        assertEquals("\u00DE\u03A9\u0403\uA779", "\u00FE\u03A9\u0453\uA77A".uppercase())
    }

    @Test
    fun indexOf() {
        var str = "Hello World!!"    // for indexOf String
        var ch = 'a'                 // for indexOf Char

        assertEquals(6, str.indexOf("World", 0))
        assertEquals(6, str.indexOf("World", -1))

        assertEquals(-1, str.indexOf(ch, 0))

        str = "Kotlin/Native"
        assertEquals(-1, str.indexOf("/", str.length + 1))
        assertEquals(-1, str.indexOf("/", Int.MAX_VALUE))
        assertEquals(str.length, str.indexOf("", Int.MAX_VALUE))
        assertEquals(1, str.indexOf("", 1))

        assertEquals(8, str.indexOf(ch, 1))
        assertEquals(-1, str.indexOf(ch, str.length - 1))

        str = ""
        assertEquals(-1, str.indexOf("a", -3))
        assertEquals(0, str.indexOf("", 0))

        assertEquals(-1, str.indexOf(ch, -3))
        assertEquals(-1, str.indexOf(ch, 10))

        ch = 0.toChar()
        assertEquals(-1, str.indexOf(ch, -3))
        assertEquals(-1, str.indexOf(ch, 10))
    }

    @Test
    fun runTest() {
        val str = "hello"
        assertTrue(str.equals("HElLo", true))
        val strI18n = "Привет"
        assertTrue(strI18n.equals("прИВет", true))
        assertEquals("ПРИВЕТ", strI18n.toUpperCase())
        assertEquals("привет", strI18n.toLowerCase())
        assertEquals("Пока", "пока".capitalize())
        assertTrue("http://jetbrains.com".startsWith("http://"))
    }

    @Test
    fun trimWhitespaces() {
        assertEquals(expected = "String", actual = "  String".trim(), message = "Trim leading spaces")
        assertEquals(expected = "String  ", actual = "    String  ".trimStart(), message = "Trim start")
        assertEquals(expected = "  String", actual = "  String \t ".trimEnd(), message = "Trim end")

        assertEquals(expected = "String", actual = "\u0020 \u202FString\u2028\u2029".trim(),
                message = "Trim special whitespaces")
        assertEquals(expected = "\u1FFFString", actual = "\u00A0  \u1FFFString".trim(),
                message = "Trim special whitespace but should left a unicode symbol")
        assertEquals(expected = "String\tSTR", actual = " \nString\tSTR  ".trim(), message = "Trim newline")
    }

    @Test
    fun testIntToStringWithRadix() {
        assertEquals(2147483647.toString(8),  "17777777777", "Octal string")
        assertEquals(2147483647.toString(16), "7fffffff", "Hex string")
        assertEquals(2147483647.toString(2),  "1111111111111111111111111111111", "Binary string")
        assertEquals(2147483647.toString(10), "2147483647", "Decimal string")

        assertEquals((-2147483647).toString(8),  "-17777777777", "Octal string")
        assertEquals((-2147483647).toString(16), "-7fffffff", "Hex string")
        assertEquals((-2147483647).toString(2),  "-1111111111111111111111111111111", "Binary string")
        assertEquals((-2147483647).toString(10), "-2147483647", "Decimal string")

        assertEquals((-2147483648).toString(8),  "-20000000000", "Octal string")
        assertEquals((-2147483648).toString(16), "-80000000", "Hex string")
        assertEquals((-2147483648).toString(2),  "-10000000000000000000000000000000", "Binary string")
        assertEquals((-2147483648).toString(10), "-2147483648", "Decimal string")
    }

    @Test
    fun testLongToStringWithRadix() {
        assertEquals(100000000L.toString(10), "100000000", "Decimal string")
        assertEquals(68719476735L.toString(16), "fffffffff", "Hex string")
        assertEquals(8589934591L.toString(8), "77777777777", "Octal string")
        assertEquals(8796093022207L.toString(2), "1111111111111111111111111111111111111111111", "Binary string")

        assertEquals((-0x7fffffffffffffffL - 1).toString(10), "-9223372036854775808", "Min decimal string")
        assertEquals(0x7fffffffffffffffL.toString(10), "9223372036854775807", "Max decimal string")
        assertEquals((-0x7fffffffffffffffL - 1).toString(16), "-8000000000000000", "Min hex string")
        assertEquals(0x7fffffffffffffffL.toString(16), "7fffffffffffffff", "Max hex string")
    }

    @Test
    fun testCase() {
        assertEquals("\ud801\udc00", "\ud801\udc28".uppercase())
        assertEquals("\ud801\udc00", "\ud801\udc00".uppercase())
        assertEquals("\ud801\udc28", "\ud801\udc28".lowercase())
        assertEquals("\ud801\udc28", "\ud801\udc00".lowercase())
    }
}
