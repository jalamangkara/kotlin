// TARGET_BACKEND: JVM
// WITH_STDLIB
// IGNORE_K1_K2_ABI_DIFFERENCE: KT-62464

// FILE: 1.kt
package test

@JvmName("jvmName")
inline fun f(s: String = "OK"): String = s

// FILE: 2.kt

fun box(): String = test.f()
