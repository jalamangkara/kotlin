// DIAGNOSTICS: -UNCHECKED_CAST

// CHECK_TYPE_WITH_EXACT

// ISSUE: KT-61907
// (also see an analogous codegen test)

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
