// IGNORE_BACKEND: NATIVE
// IGNORE_BACKEND: WASM
// IGNORE_BACKEND: JS, JS_IR, JS_IR_ES6
// IGNORE_LIGHT_ANALYSIS

// CHECK_TYPE_WITH_EXACT

class Buildee<CT> {
    fun box(): Box<CT> = Box()
}

fun <FT> build(
    instructions: Buildee<FT>.() -> Unit
): Buildee<FT> {
    return Buildee<FT>().apply(instructions)
}

class UserKlass
class Box<T>

val Box<UserKlass>.immutableTypeInfoSource: Box<UserKlass>
    get() = this
var Box<UserKlass>.mutableTypeInfoSource: Box<UserKlass>
    get() = this
    set(value) {}

fun testImmutableProperty() {
    val buildee = build {
        box().immutableTypeInfoSource
    }
    checkExactType<Buildee<UserKlass>>(buildee)
}

fun testMutableProperty() {
    val buildee = build {
        box().mutableTypeInfoSource
    }
    checkExactType<Buildee<UserKlass>>(buildee)
}

fun box(): String {
    testImmutableProperty()
    testMutableProperty()
    return "OK"
}
