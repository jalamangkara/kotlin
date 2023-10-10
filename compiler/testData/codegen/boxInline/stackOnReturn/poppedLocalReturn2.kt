// IGNORE_K1_K2_ABI_DIFFERENCE: KT-62464

// FILE: 1.kt
inline fun alwaysOk(s: String, fn: (String) -> String): String {
    return fn(try {return "OK"} catch (e: Exception) {
        "fail1"
    }) + fn(try {return "FF"} catch (e: Exception) {
        "fail2"
    })
}
// FILE: 2.kt
fun box() = alwaysOk("what?") { it }
