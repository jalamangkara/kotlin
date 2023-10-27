/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.generators.tree

import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.generators.tree.printer.*
import org.jetbrains.kotlin.utils.SmartPrinter
import org.jetbrains.kotlin.utils.withIndent

/**
 * A common class for printing FIR or IR tree elements.
 */
abstract class AbstractElementPrinter<Element : AbstractElement<Element, Field>, Field : AbstractField>(
    private val printer: SmartPrinter,
) {

    protected abstract fun makeFieldPrinter(printer: SmartPrinter): AbstractFieldPrinter<Field>

    context(ImportCollector)
    protected abstract fun SmartPrinter.printAdditionalMethods(element: Element)

    protected open fun defaultElementKDoc(element: Element): String? = null

    protected open val separateFieldsWithBlankLine: Boolean
        get() = false

    context(ImportCollector)
    fun printElement(element: Element) {
        printer.run {
            val kind = element.kind ?: error("Expected non-null element kind")

            printKDoc(element.extendedKDoc(defaultElementKDoc(element)))
            print(kind.title, " ", element.typeName)
            print(element.params.typeParameters())

            val parentRefs = element.parentRefs
            if (parentRefs.isNotEmpty()) {
                print(
                    parentRefs.sortedBy { it.typeKind }.joinToString(prefix = " : ") { parent ->
                        parent.render() + parent.inheritanceClauseParenthesis()
                    }
                )
            }
            print(element.params.multipleUpperBoundsList())

            val body = SmartPrinter(StringBuilder()).apply {
                withIndent {
                    for (field in element.allFields) {
                        if (
                            !field.withGetter && field.defaultValueInImplementation == null && field.isFinal && field.fromParent ||
                            field.isParameter
                        ) {
                            continue
                        }
                        if (separateFieldsWithBlankLine) println()
                        makeFieldPrinter(this).printField(
                            field,
                            override = field.fromParent,
                            modality = Modality.ABSTRACT.takeIf { !field.isFinal && !kind.isInterface },
                        )
                    }
                    printAdditionalMethods(element)
                }
            }.toString()

            if (body.isNotEmpty()) {
                println(" {")
                print(body)
                print("}")
            }
            println()
        }
    }
}
