// DIAGNOSTICS: -UNCHECKED_CAST

// CHECK_TYPE_WITH_EXACT

// ISSUE: KT-59369
/* ATTENTION:
 * this test is affected by a more general compielr bug at the moment;
 * if the behavior of the test remains incorrect after the aforementioned bug is fixed,
 * please create a new ticket and notify somebody either from
 * Kotlin Compiler Core QA or Kotlin Language Evolution teams
 */

class Buildee<CT> {
    fun materialize(): CT = UserKlass() as CT
}

fun <FT> build(
    instructions: Buildee<FT>.() -> Unit
): Buildee<FT> {
    return Buildee<FT>().apply(instructions)
}

class UserKlass

val UserKlass.immutableTypeInfoSource: UserKlass
    get() = this
var UserKlass.mutableTypeInfoSource: UserKlass
    get() = this
    set(value) {}

fun testImmutableProperty() {
    val buildee = build {
        // postponed type variable values cannot be used as receivers
        materialize().immutableTypeInfoSource
    }
    checkExactType<Buildee<UserKlass>>(buildee)
}

fun testMutableProperty() {
    val buildee = build {
        // postponed type variable values cannot be used as receivers
        materialize().mutableTypeInfoSource
    }
    checkExactType<Buildee<UserKlass>>(buildee)
}
