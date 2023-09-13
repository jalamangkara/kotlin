// DIAGNOSTICS: -UNCHECKED_CAST

// CHECK_TYPE_WITH_EXACT

// ISSUE: KT-61909
// (also see an analogous codegen test)

class Buildee<CT>

var <CT> Buildee<CT>.variable: CT
    get() = UserKlass() as CT
    set(value) {}

fun <FT> build(
    instructions: Buildee<FT>.() -> Unit
): Buildee<FT> {
    return Buildee<FT>().apply(instructions)
}

class UserKlass

fun testYield() {
    val arg: UserKlass = UserKlass()
    val buildee = <!INFERRED_INTO_DECLARED_UPPER_BOUNDS!>build<!> {
        variable = arg
    }
    checkExactType<Buildee<UserKlass>>(<!TYPE_MISMATCH("Buildee<UserKlass>; Buildee<Any?>"), TYPE_MISMATCH("Buildee<Any?>; Buildee<UserKlass>")!>buildee<!>)
}
