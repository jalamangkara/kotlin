// !OPT_IN: kotlin.contracts.ExperimentalContracts
// NO_CHECK_LAMBDA_INLINING
// IGNORE_K1_K2_ABI_DIFFERENCE: KT-62464

// FILE: 1.kt

package test

import kotlin.contracts.*

public inline fun myrun(noinline block: () -> Unit): Unit {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    block()
}

// FILE: 2.kt

import test.*

fun box(): String {
    val x: Long
    myrun {
        x = 42L
    }
    return if (x != 42L) "FAIL" else "OK"
}
