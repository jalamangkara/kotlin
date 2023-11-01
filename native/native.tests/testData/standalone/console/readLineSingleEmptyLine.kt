// KIND: STANDALONE_NO_TR
// INPUT_DATA_FILE: readLineSingleEmptyLine.in

import kotlin.test.*

fun main() {
    assertEquals("", readLine())
    assertNull(readLine())
}