// WITH_STDLIB
// WITH_REFLECT
// TARGET_BACKEND: JVM_IR

class Test {
    @ClassObjectAnnotation
    companion object {
        annotation class ClassObjectAnnotation
    }
}

fun box() = "OK".also {
    Test.Companion
}
