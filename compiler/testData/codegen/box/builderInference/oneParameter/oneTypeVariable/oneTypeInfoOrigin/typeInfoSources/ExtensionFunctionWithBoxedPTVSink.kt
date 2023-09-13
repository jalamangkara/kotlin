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

fun Box<UserKlass>.typeInfoSource() {}

fun box(): String {
    val buildee = build {
        box().typeInfoSource()
    }
    checkExactType<Buildee<UserKlass>>(buildee)
    return "OK"
}
