/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.native.ref

import kotlin.concurrent.AtomicInt
import kotlin.native.internal.waitCleanerWorker
import kotlin.native.ref.*
import kotlin.native.runtime.GC
import kotlin.test.*

private class AtomicBoolean(initial: Boolean) {
    private val impl = AtomicInt(if (initial) 1 else 0)

    var value: Boolean
        get() = impl.value != 0
        set(value) {
            impl.value = if (value) 1 else 0
        }
}

private class FunBox(private val impl: () -> Unit) {
    fun call() {
        impl()
    }
}

private val globalInt = AtomicInt(0)

private const val DEFAULT_TLS_VALUE = 11
@ThreadLocal
private var tlsValue = DEFAULT_TLS_VALUE

class CleanerTest {
    @Test
    fun lambda() {
        val called = AtomicBoolean(false)

        // This relies on local vars disappearing from the root set after this function exits.
        fun create(): Pair<WeakReference<FunBox>, WeakReference<Cleaner>> {
            val funBox = FunBox { called.value = true }
            val cleaner = createCleaner(funBox) { it.call() }
            return WeakReference(funBox) to WeakReference(cleaner)
        }

        val (funBoxWeak, cleanerWeak) = create()

        assertFalse(called.value)

        GC.collect() // Run first GC and wait for the finalizers.
        assertNull(cleanerWeak!!.value)
        waitCleanerWorker() // And make sure the cleaners are finished.
        assertTrue(called.value)
        GC.collect() // FunBox was detached from the cleaner earlier, and can be collected on this iteration.
        assertNull(funBoxWeak!!.value)
    }

    @Test
    fun lambdaWithException() {
        val called = AtomicBoolean(false)

        // This relies on local vars disappearing from the root set after this function exits.
        fun create(): Pair<WeakReference<FunBox>, WeakReference<Cleaner>> {
            val funBox = FunBox { called.value = true }
            val cleaner = createCleaner(funBox) {
                it.call()
                error("Cleaner failed")
            }
            return WeakReference(funBox) to WeakReference(cleaner)
        }

        val (funBoxWeak, cleanerWeak) = create()

        assertFalse(called.value)

        GC.collect() // Run first GC and wait for the finalizers.
        assertNull(cleanerWeak!!.value)
        waitCleanerWorker() // And make sure the cleaners are finished.
        assertTrue(called.value)
        GC.collect() // FunBox was detached from the cleaner earlier, and can be collected on this iteration.
        assertNull(funBoxWeak!!.value)
    }

    @Test
    fun int() {
        // This relies on local vars disappearing from the root set after this function exits.
        fun create(): WeakReference<Cleaner> {
            val cleaner = createCleaner(42) { globalInt.value = it }
            return WeakReference(cleaner)
        }

        val cleanerWeak = create()

        assertEquals(0, globalInt.value)

        GC.collect() // Run first GC and wait for the finalizers.
        assertNull(cleanerWeak!!.value)
        waitCleanerWorker() // And make sure the cleaners are finished.
        assertEquals(42, globalInt.value)
    }

    @Test
    fun anonymousFunction() {
        val called = AtomicBoolean(false)

        // This relies on local vars disappearing from the root set after this function exits.
        fun create(): Pair<WeakReference<FunBox>, WeakReference<Cleaner>> {
            val funBox = FunBox { called.value = true }
            val cleaner = createCleaner(funBox, fun (it: FunBox) { it.call() })
            return WeakReference(funBox) to WeakReference(cleaner)
        }

        val (funBoxWeak, cleanerWeak) = create()

        assertFalse(called.value)

        GC.collect() // Run first GC and wait for the finalizers.
        assertNull(cleanerWeak!!.value)
        waitCleanerWorker() // And make sure the cleaners are finished.
        assertTrue(called.value)
        GC.collect() // FunBox was detached from the cleaner earlier, and can be collected on this iteration.
        assertNull(funBoxWeak!!.value)
    }


    @Test
    fun functionReference() {
        val called = AtomicBoolean(false)

        // This relies on local vars disappearing from the root set after this function exits.
        fun create(): Pair<WeakReference<FunBox>, WeakReference<Cleaner>> {
            val funBox = FunBox { called.value = true }
            val cleaner = createCleaner(funBox, FunBox::call)
            return WeakReference(funBox) to WeakReference(cleaner)
        }

        val (funBoxWeak, cleanerWeak) = create()

        assertFalse(called.value)

        GC.collect() // Run first GC and wait for the finalizers.
        assertNull(cleanerWeak!!.value)
        waitCleanerWorker() // And make sure the cleaners are finished.
        assertTrue(called.value)
        GC.collect() // FunBox was detached from the cleaner earlier, and can be collected on this iteration.
        assertNull(funBoxWeak!!.value)
    }

    @Test
    fun cleanerHasSeparateThread() {
        tlsValue = DEFAULT_TLS_VALUE + 1
        val cleanerTlsValue = AtomicInt(DEFAULT_TLS_VALUE + 2)

        // This relies on local vars disappearing from the root set after this function exits.
        fun create(): WeakReference<Cleaner> {
            val cleaner = createCleaner(cleanerTlsValue) { it.value = tlsValue }
            return WeakReference(cleaner)
        }

        val cleanerWeak = create()

        assertEquals(DEFAULT_TLS_VALUE + 2, cleanerTlsValue.value)

        GC.collect() // Run first GC and wait for the finalizers.
        assertNull(cleanerWeak!!.value)
        waitCleanerWorker() // And make sure the cleaners are finished.
        assertEquals(DEFAULT_TLS_VALUE, cleanerTlsValue.value)
    }
}