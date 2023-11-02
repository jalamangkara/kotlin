/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.lazy

import org.jetbrains.kotlin.bir.BirChildElementList
import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.BirElementBase
import org.jetbrains.kotlin.ir.IrElement

class BirLazyChildElementList<E : BirElement?>(
    override val parent: BirLazyElementBase,
    id: Int,
    isNullable: Boolean,
) : BirChildElementList<E>(id, isNullable) {
    private var upstreamList: List<IrElement>? = null

    override val size: Int
        get() {
            queryUpstreamList()
            return _size
        }

    override fun get(index: Int): E {
        val upstreamList = queryUpstreamList()
        checkElementIndex(index, _size)

        var elementArray = elementArray
        var element = if (elementArray.isNotEmpty()) elementArray[index] else null
        if (element == null) {
            if (elementArray.isEmpty()) {
                elementArray = arrayOfNulls<BirElementBase>(_size)
                this.elementArray = elementArray
            }

            val originalElement = upstreamList[index]
            element = parent.converter.remapElement<BirElementBase>(originalElement)
            elementArray[index] = element
            element.setContainingList()
        }

        @Suppress("UNCHECKED_CAST")
        return element as E
    }

    private fun queryUpstreamList(): List<IrElement> {
        return upstreamList ?: parent.getOriginalChildList(this).also {
            upstreamList = it
            _size = it.size
        }
    }


    override fun set(index: Int, element: E): E {
        BirLazyElementBase.mutationNotSupported()
    }

    override fun add(element: E): Boolean {
        BirLazyElementBase.mutationNotSupported()
    }

    override fun add(index: Int, element: E) {
        BirLazyElementBase.mutationNotSupported()
    }

    override fun addAll(index: Int, elements: Collection<E>): Boolean {
        BirLazyElementBase.mutationNotSupported()
    }

    override fun addAll(elements: Collection<E>): Boolean {
        BirLazyElementBase.mutationNotSupported()
    }

    override fun removeAt(index: Int): E {
        BirLazyElementBase.mutationNotSupported()
    }

    override fun retainAll(elements: Collection<E>): Boolean {
        BirLazyElementBase.mutationNotSupported()
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        BirLazyElementBase.mutationNotSupported()
    }

    override fun remove(element: E): Boolean {
        BirLazyElementBase.mutationNotSupported()
    }

    override fun clear() {
        BirLazyElementBase.mutationNotSupported()
    }


    override fun iterator(): MutableIterator<E> {
        TODO("Not yet implemented")
    }

    override fun listIterator(): MutableListIterator<E> {
        TODO("Not yet implemented")
    }

    override fun listIterator(index: Int): MutableListIterator<E> {
        TODO("Not yet implemented")
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> {
        TODO("Not yet implemented")
    }
}