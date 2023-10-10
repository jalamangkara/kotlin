// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses
// IGNORE_K1_K2_ABI_DIFFERENCE: KT-57268

OPTIONAL_JVM_INLINE_ANNOTATION
value class Wrapper(val id: Int)

class DMap(private val map: Map<Wrapper, String>) :
        Map<Wrapper, String> by map

fun box(): String {
    val dmap = DMap(mutableMapOf(Wrapper(42) to "OK"))
    return dmap[Wrapper(42)] ?: "Fail"
}