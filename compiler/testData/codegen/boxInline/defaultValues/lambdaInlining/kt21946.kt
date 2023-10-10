// NO_CHECK_LAMBDA_INLINING
// IGNORE_K1_K2_ABI_DIFFERENCE: KT-62464

// FILE: 1.kt
package test

interface Call {
    fun run(): String
}

inline fun test(p: String, s: () -> Call = {
    object : Call {
        override fun run() = p
    }
}) = s()

// FILE: 2.kt
import test.*

fun box(): String {
    return test("OK").run()
}
