// MODULE: m1-common
// FILE: common.kt

open class Base {
    open var foo: String = ""
        protected set
}

<!INCOMPATIBLE_MATCHING{JVM}, INCOMPATIBLE_MATCHING{JVM}!>expect open class Foo : Base<!>

// MODULE: m2-jvm()()(m1-common)
// FILE: jvm.kt

actual open class Foo : Base() {
    override var foo: String = ""
        public set
}
