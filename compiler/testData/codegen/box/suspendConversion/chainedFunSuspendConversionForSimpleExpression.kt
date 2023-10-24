// !LANGUAGE: +SuspendConversion
// IGNORE_BACKEND: JVM
// IGNORE_K1_K2_ABI_DIFFERENCE: KT-62855

fun interface SuspendRunnable {
    suspend fun invoke()
}

fun foo(s: SuspendRunnable) {}

fun test(f: () -> Unit) {
    foo { }
    foo(f)
}

fun box(): String {
    test({ "" })
    return "OK"
}