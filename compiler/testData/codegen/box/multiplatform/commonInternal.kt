// TARGET_BACKEND: JVM
// IGNORE_K1_K2_ABI_DIFFERENCE: KT-62908

// MODULE: common
// FILE: commonMain.kt
internal annotation class AnnInCommon

// MODULE: friend
// FILE: friendMain.kt
internal annotation class AnnInFriend

// MODULE: main()(friend)(common)
// FILE: main.kt
@AnnInCommon
@AnnInFriend
class A

fun box() = "OK"
