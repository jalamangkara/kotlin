// !OPT_IN: kotlin.js.ExperimentalJsExport
// !DIAGNOSTICS: -UNUSED_PARAMETER
// !RENDER_DIAGNOSTICS_MESSAGES

package foo

class C

@JsExport
fun foo(x: C) {
}

@JsExport
fun bar() = C()

@JsExport
val x: C = C()

@JsExport
var x2: C
    get() = C()
    set(value) { }

@JsExport
class A(
    val x: C,
    y: C
) {
    fun foo(x: C) = x

    val x2: C = C()

    var x3: C
        get() = C()
        set(value) { }
}

@JsExport
fun foo2() {
}

@JsExport
fun foo3(x: Unit) {
}

@JsExport
fun foo4(x: () -> Unit) {
}

@JsExport
fun foo5(x: (Unit) -> Unit) {
}

@JsExport
fun foo6(x: (A) -> A) {
}

@JsExport
fun foo7(x: List<Int>) {
}

@JsExport
fun foo8(x: MutableList<Int>) {
}

@JsExport
fun foo9(x: Set<Int>) {
}

@JsExport
fun foo10(x: MutableSet<Int>) {
}

@JsExport
fun foo11(x: Map<String, Int>) {
}

@JsExport
fun foo12(x: MutableMap<String, Int>) {
}
