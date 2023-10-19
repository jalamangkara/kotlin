// IGNORE_K1_K2_ABI_DIFFERENCE: KT-62714

// MODULE: lib
// FILE: lib.kt
enum class A {
    X {
        val x = "OK"

        inner class Inner {
            val y = x
        }

        val z = Inner()

        override val test: String
            get() = z.y
    };

    abstract val test: String
}

// MODULE: main(lib)
// FILE: main.kt
fun box() = A.X.test
