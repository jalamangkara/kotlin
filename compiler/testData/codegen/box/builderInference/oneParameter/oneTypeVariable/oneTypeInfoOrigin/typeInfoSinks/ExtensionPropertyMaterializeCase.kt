// IGNORE_BACKEND: NATIVE
// IGNORE_BACKEND: WASM
// IGNORE_BACKEND: JS, JS_IR, JS_IR_ES6
// IGNORE_LIGHT_ANALYSIS

// CHECK_TYPE_WITH_EXACT

class Buildee<CT>

val <CT> Buildee<CT>.value: CT
    get() = UserKlass() as CT
var <CT> Buildee<CT>.variable: CT
    get() = UserKlass() as CT
    set(value) {}

fun <FT> build(
    instructions: Buildee<FT>.() -> Unit
): Buildee<FT> {
    return Buildee<FT>().apply(instructions)
}

class UserKlass

fun testMaterialize() {
    fun testImmutableProperty() {
        fun consume(arg: UserKlass) {}
        val buildee = build {
            consume(value)
        }
        checkExactType<Buildee<UserKlass>>(buildee)
    }

    fun testMutableProperty() {
        fun consume(arg: UserKlass) {}
        val buildee = build {
            consume(variable)
        }
        checkExactType<Buildee<UserKlass>>(buildee)
    }

    testImmutableProperty()
    testMutableProperty()
}

fun box(): String {
    testMaterialize()
    return "OK"
}
