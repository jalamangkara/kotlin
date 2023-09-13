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
    val buildee = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>build<!> {
        variable = <!TYPE_MISMATCH("TypeVariable(FT); UserKlass")!>arg<!>
    }
    checkExactType<Buildee<UserKlass>>(<!DEBUG_INFO_ELEMENT_WITH_ERROR_TYPE!>buildee<!>)
}
