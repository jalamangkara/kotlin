// MODULE: m1-common
// FILE: common.kt
<!INCOMPATIBLE_MATCHING{JVM}!>expect class Foo {
    <!INCOMPATIBLE_MATCHING{JVM}!>class Inner<!INCOMPATIBLE_MATCHING{JVM}!>()<!><!>
}<!>

<!INCOMPATIBLE_MATCHING{JVM}!>expect class SeveralInner {
    <!INCOMPATIBLE_MATCHING{JVM}!>class Inner1 {
        <!INCOMPATIBLE_MATCHING{JVM}!>class Inner2 {
            <!INCOMPATIBLE_MATCHING{JVM}!>class Inner3<!INCOMPATIBLE_MATCHING{JVM}!>()<!><!>
        }<!>
    }<!>
}<!>

// MODULE: m2-jvm()()(m1-common)
// FILE: jvm.kt
class FooImpl {
    class Inner
}

actual typealias Foo = FooImpl

class SeveralInnerImpl {
    class Inner1 {
        class Inner2 {
            class Inner3
        }
    }
}

actual typealias SeveralInner = SeveralInnerImpl
