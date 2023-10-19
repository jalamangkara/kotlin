// TARGET_BACKEND: JVM
// ALLOW_KOTLIN_PACKAGE
// IGNORE_K1_K2_ABI_DIFFERENCE: KT-62750
// Note: order of files is important

// MODULE: kotlin_stdlib
// FILE: _Arrays.kt
package kotlin.collections

public inline fun ByteArray.customFirst(predicate: (Byte) -> Boolean): Byte {
    val iter = this.iterator()
    while (iter.hasNext()) {
        val element = iter.next()
        if (predicate(element)) return element
    }
    throw NoSuchElementException("Array contains no element matching the predicate.")
}

// FILE: PrimitiveIterators.kt
package kotlin.collections

public abstract class ByteIterator : Iterator<Byte> {
    override final fun next() = nextByte()

    /** Returns the next value in the sequence without boxing. */
    public abstract fun nextByte(): Byte
}

// MODULE: main(kotlin_stdlib)
// FILE: main.kt
fun box(): String {
    byteArrayOf(1, 2, 3).customFirst { it == 1.toByte() }
    return "OK"
}
