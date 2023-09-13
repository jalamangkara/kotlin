// IGNORE_BACKEND_K2: NATIVE
// IGNORE_BACKEND_K2: WASM
// IGNORE_BACKEND_K2: JS, JS_IR, JS_IR_ES6
// IGNORE_LIGHT_ANALYSIS

// CHECK_TYPE_WITH_EXACT

// IGNORE_BACKEND_K1: ANY
// ISSUE: KT-61907

class Buildee<CT> {
    var variable: CT = UserKlass() as CT
}

fun <FT> build(
    instructions: Buildee<FT>.() -> Unit
): Buildee<FT> {
    return Buildee<FT>().apply(instructions)
}

class UserKlass

fun testYield() {
    val arg: UserKlass = UserKlass()
    val buildee = build {
        variable = arg
    }
    checkExactType<Buildee<UserKlass>>(buildee)
}

fun box(): String {
    testYield()
    return "OK"
}
