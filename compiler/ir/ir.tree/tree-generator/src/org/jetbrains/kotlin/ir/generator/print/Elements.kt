/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.generator.print

import org.jetbrains.kotlin.generators.tree.*
import org.jetbrains.kotlin.generators.tree.printer.*
import org.jetbrains.kotlin.ir.generator.BASE_PACKAGE
import org.jetbrains.kotlin.ir.generator.TREE_GENERATOR_README
import org.jetbrains.kotlin.ir.generator.elementTransformerType
import org.jetbrains.kotlin.ir.generator.elementVisitorType
import org.jetbrains.kotlin.ir.generator.model.*
import org.jetbrains.kotlin.utils.SmartPrinter
import org.jetbrains.kotlin.utils.withIndent
import java.io.File
import org.jetbrains.kotlin.generators.tree.ElementRef as GenericElementRef

private val transformIfNeeded = ArbitraryImportable("$BASE_PACKAGE.util", "transformIfNeeded")
private val transformInPlace = ArbitraryImportable("$BASE_PACKAGE.util", "transformInPlace")

private class ElementPrinter(printer: SmartPrinter) : AbstractElementPrinter<Element, Field>(printer) {

    override fun makeFieldPrinter(printer: SmartPrinter) = object : AbstractFieldPrinter<Field>(printer) {
        override fun forceMutable(field: Field) = field.isMutable
    }

    override fun defaultElementKDoc(element: Element) =
        "A ${if (element.isLeaf) "leaf" else "non-leaf"} IR tree element."

    override val separateFieldsWithBlankLine: Boolean
        get() = true

    context(ImportCollector)
    override fun SmartPrinter.printAdditionalMethods(element: Element) {

        if (element.hasAcceptMethod) {
            printAcceptMethod(
                element = element,
                visitorClass = elementVisitorType,
                hasImplementation = !element.isRootElement,
                kDoc = """
                Runs the provided [visitor] on the IR subtree with the root at this node.
                
                @param visitor The visitor to accept.
                @param data An arbitrary context to pass to each invocation of [visitor]'s methods.
                @return The value returned by the topmost `visit*` invocation.
                """.trimIndent().takeIf { element.isRootElement }
            )
        }

        if (element.hasTransformMethod) {
            printTransformMethod(
                element = element,
                transformerClass = elementTransformerType,
                implementation = "accept(transformer, data)".takeIf { !element.isRootElement },
                returnType = element,
                kDoc = """
                    Runs the provided [transformer] on the IR subtree with the root at this node.
        
                    @param transformer The transformer to use.
                    @param data An arbitrary context to pass to each invocation of [transformer]'s methods.
                    @return The transformed node.
                    """.trimIndent().takeIf { element.isRootElement }
            )
        }

        if (element.hasAcceptChildrenMethod) {
            printAcceptChildrenMethod(
                element = element,
                visitorClass = elementVisitorType,
                visitorResultType = StandardTypes.unit,
                override = !element.isRootElement,
                kDoc = """
                    Runs the provided [visitor] on subtrees with roots in this node's children.
                    
                    Basically, calls `accept(visitor, data)` on each child of this node.
                    
                    Does **not** run [visitor] on this node itself.
                    
                    @param visitor The visitor for children to accept.
                    @param data An arbitrary context to pass to each invocation of [visitor]'s methods.
                    """.trimIndent().takeIf { element.isRootElement }
            )

            if (!element.isRootElement) {
                println(" {")
                withIndent {
                    for (child in element.walkableChildren) {
                        print(child.name)
                        if (child.nullable) {
                            print("?")
                        }
                        when (child) {
                            is SingleField -> println(".accept(visitor, data)")
                            is ListField -> {
                                print(".forEach { it")
                                if (child.elementType.nullable) {
                                    print("?")
                                }
                                println(".accept(visitor, data) }")
                            }
                        }
                    }
                }
                print("}")
            }
            println()
        }

        if (element.hasTransformChildrenMethod) {
            printTransformChildrenMethod(
                element = element,
                transformerClass = elementTransformerType,
                returnType = StandardTypes.unit,
                override = !element.isRootElement,
                kDoc = """
                    Recursively transforms this node's children *in place* using [transformer].
                    
                    Basically, executes `this.child = this.child.transform(transformer, data)` for each child of this node.
                    
                    Does **not** run [transformer] on this node itself.
                    
                    @param transformer The transformer to use for transforming the children.
                    @param data An arbitrary context to pass to each invocation of [transformer]'s methods.
                    """.trimIndent().takeIf { element.isRootElement }
            )
            if (!element.isRootElement) {
                println(" {")
                withIndent {
                    for (child in element.transformableChildren) {
                        print(child.name)
                        when (child) {
                            is SingleField -> {
                                print(" = ", child.name)
                                if (child.nullable) {
                                    print("?")
                                }
                                print(".transform(transformer, data)")
                                val elementRef = child.typeRef as GenericElementRef<*, *>
                                if (!elementRef.element.hasTransformMethod) {
                                    print(" as ", elementRef.render())
                                }
                                println()
                            }
                            is ListField -> {
                                if (child.isMutable) {
                                    print(" = ", child.name)
                                    if (child.nullable) {
                                        print("?")
                                    }
                                    addImport(transformIfNeeded)
                                    println(".transformIfNeeded(transformer, data)")
                                } else {
                                    addImport(transformInPlace)
                                    println(".transformInPlace(transformer, data)")
                                }
                            }
                        }
                    }
                }
                print("}")
            }
            println()
        }

        element.generationCallback?.invoke(this@ImportCollector, this)
    }
}

fun printElements(generationPath: File, model: Model) = model.elements.map { element ->
    printGeneratedType(generationPath, TREE_GENERATOR_README, element.packageName, element.typeName) {
        addAllImports(element.usedTypes)
        ElementPrinter(this).printElement(element)
    }
}
