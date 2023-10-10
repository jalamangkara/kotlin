// TARGET_BACKEND: JVM
// IGNORE_K1_K2_ABI_DIFFERENCE: KT-57268

import java.util.AbstractMap
import java.util.Collections

class A : AbstractMap<Int, String>() {
    override val entries: MutableSet<MutableMap.MutableEntry<Int, String>> get() = Collections.emptySet()
}

fun box(): String {
    val a = A()
    val b = A()

    a.remove(0)

    a.putAll(b)
    a.clear()

    a.keys
    a.values
    a.entries

    return "OK"
}
