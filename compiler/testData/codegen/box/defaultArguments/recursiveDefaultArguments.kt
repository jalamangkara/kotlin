// IGNORE_BACKEND: JS
// IGNORE_K1_K2_ABI_DIFFERENCE: KT-62464

@Suppress("RECURSION_IN_INLINE")
inline fun test(p: String = test("OK")): String {
    return p
}

fun box() : String {
    return test()
}