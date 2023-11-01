// KIND: STANDALONE_NO_TR
// INPUT_DATA_FILE: readlnEmpty.in

import kotlin.test.*

fun main() {
    assertFailsWith<RuntimeException> { readln() }
    assertFailsWith<RuntimeException> { readln() }
}