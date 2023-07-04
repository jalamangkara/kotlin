// ORIGINAL: /compiler/testData/diagnostics/testsWithStdLib/coroutines/suspendTest.fir.kt
// WITH_STDLIB
// ALLOW_KOTLIN_PACKAGE
// SKIP_TXT
// FILE: test.kt

package kotlin.test

annotation class IrrelevantClass

public typealias Test = IrrelevantClass

// FILE: main.kt

import kotlin.test.Test

class A {
    @Test
    suspend fun test() {}
}

@Test
suspend fun test() {}


fun box() = "OK".also { test() }
