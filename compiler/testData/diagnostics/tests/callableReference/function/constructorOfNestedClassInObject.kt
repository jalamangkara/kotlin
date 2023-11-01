// KT-15951 Callable reference to class constructor from object is not resolved
// KT-63069

object A {
    class Wrapper
}

class Outer {
    companion object {
        class Wrapper
    }
}

fun test() {
    A::Wrapper
    (A)::<!UNRESOLVED_REFERENCE!>Wrapper<!>

    Outer.Companion::Wrapper
    (Outer.Companion)::<!UNRESOLVED_REFERENCE!>Wrapper<!>
    Outer::<!UNRESOLVED_REFERENCE!>Wrapper<!>
    (Outer)::<!UNRESOLVED_REFERENCE!>Wrapper<!>
}
