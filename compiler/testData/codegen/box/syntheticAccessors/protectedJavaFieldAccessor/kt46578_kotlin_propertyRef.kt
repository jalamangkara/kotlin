// TARGET_BACKEND: JVM
// WITH_STDLIB
// IGNORE_K1_K2_ABI_DIFFERENCE: KT-62516

// FILE: kt46578_kotlin_propertyRef.kt
import p.*

class Derived : Base() {
    init {
        pf = "OK"
    }
    val ref = ::pf
}

fun box(): String {
    return Derived().ref.get()
}

// FILE: p/Base.kt
package p

open class Base {
    protected var pf = ""
}
