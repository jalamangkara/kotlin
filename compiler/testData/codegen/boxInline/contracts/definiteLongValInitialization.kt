// !OPT_IN: kotlin.contracts.ExperimentalContracts
// IGNORE_K1_K2_ABI_DIFFERENCE: KT-62464

// FILE: 1.kt

package test

import kotlin.contracts.*

public inline fun <R> myrun(block: () -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return block()
}

// FILE: 2.kt

import test.*

fun box(): String {
    val x: Long
    myrun {
        x = 42L
    }
    return if (x.inc() == 43L) "OK" else "Fail: ${x.inc()}"
}
