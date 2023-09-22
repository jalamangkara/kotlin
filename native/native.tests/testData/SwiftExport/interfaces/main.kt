package interfaces

interface Foo {
    val foo: Int
    fun bar(): Double

    interface Baz: Foo {}
}

interface Bar: Foo {
    var baz: Long
}