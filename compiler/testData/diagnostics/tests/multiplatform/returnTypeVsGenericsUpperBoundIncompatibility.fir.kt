// MODULE: m1-common
// FILE: common.kt

interface A
<!INCOMPATIBLE_MATCHING{JVM}!>expect fun <T : A> foo(t: T): String<!>

// MODULE: m2-jvm()()(m1-common)
// FILE: jvm.kt

fun <T : A> foo(t: T): T = TODO()
fun <T> foo(t: T): String = TODO()
