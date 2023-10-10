// !OPT_IN: kotlin.contracts.ExperimentalContracts

// IGNORE_K1_K2_ABI_DIFFERENCE: KT-62464

import kotlin.contracts.*

inline fun foo(x: () -> String, y: () -> String): String {
    contract {
        callsInPlace(x, InvocationKind.EXACTLY_ONCE)
        callsInPlace(y, InvocationKind.EXACTLY_ONCE)
    }
    return x() + y()
}

fun box(): String {
    val y = { "K" }
    return foo({ "O" }, y)
}
