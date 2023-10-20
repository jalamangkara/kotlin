// TARGET_BACKEND: JVM_IR
// IGNORE_K1_K2_ABI_DIFFERENCE: KT-55017

// FILE: base/Jaba.java

package base;

public class Jaba {
    protected String a = "FAIL";
}

// FILE: test.kt

import base.Jaba

fun box(): String {
    val x = object : Jaba() {
        private val a: String = "OK"
        inner class S {
            fun foo() = ::a.get()
        }

        fun bar() = S().foo()
    }

    return x.bar()
}
