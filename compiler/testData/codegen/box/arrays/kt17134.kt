// WITH_STDLIB
// TARGET_BACKEND: JVM

// IGNORE_K1_K2_ABI_DIFFERENCE: KT-62465

object A {
    @JvmStatic fun main(args: Array<String>) {
        val b = arrayOf(arrayOf(""))
        object {
            val c = b[0]
        }
    }
}

fun box(): String {
    A.main(emptyArray())
    return "OK"
}