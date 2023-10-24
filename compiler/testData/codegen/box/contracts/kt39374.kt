// WITH_STDLIB
// WITH_COROUTINES
// IGNORE_K1_K2_ABI_DIFFERENCE: KT-62845

import kotlin.contracts.*
import kotlin.coroutines.*
import helpers.*

@ExperimentalContracts
public fun <T> runBlocking(block: suspend () -> T): T {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    var res: T? = null
    suspend {
        res = block()
    }.startCoroutine(EmptyContinuation)
    return res!!
}

sealed class S {
    class Z : S() {
        fun f(): String = "OK"
    }
}

val z: S = S.Z()

@ExperimentalContracts
fun box(): String = when (val w = z) {
    is S.Z -> runBlocking { w.f() }
}
