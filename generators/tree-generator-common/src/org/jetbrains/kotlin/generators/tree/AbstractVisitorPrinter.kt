/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.generators.tree

import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.generators.tree.printer.FunctionParameter
import org.jetbrains.kotlin.generators.tree.printer.multipleUpperBoundsList
import org.jetbrains.kotlin.generators.tree.printer.printFunctionDeclaration
import org.jetbrains.kotlin.generators.tree.printer.typeParameters
import org.jetbrains.kotlin.types.Variance
import org.jetbrains.kotlin.utils.SmartPrinter
import org.jetbrains.kotlin.utils.withIndent

abstract class AbstractVisitorPrinter<Element : AbstractElement<Element, Field>, Field : AbstractField>(
    val printer: SmartPrinter,
    val visitSuperTypeByDefault: Boolean,
) {

    /**
     * The visitor type to print.
     */
    abstract val visitorType: ClassRef<*>

    /**
     * The result type parameter of the visitor. All visitor methods return result of this type.
     */
    protected val resultTypeVariable = TypeVariable("R", emptyList(), Variance.OUT_VARIANCE)

    /**
     * The data type parameter of the visitor. ALl visitor methods accept a parameter of this type.
     */
    protected val dataTypeVariable = TypeVariable("D", emptyList(), Variance.IN_VARIANCE)

    /**
     * The type parameters of the visitor class. Void visitors have no type parameters,
     * regular visitors usually have [resultTypeVariable] and [dataTypeVariable] here.
     */
    abstract val visitorTypeParameters: List<TypeVariable>

    /**
     * The superclass for this visitor class.
     */
    abstract val visitorSuperType: ClassRef<PositionTypeParameterRef>?

    /**
     * If `true`, visitor methods for generic tree elements will be parameterized correspondingly.
     * Otherwise, type arguments of generic tree elements will be replaced with `*`.
     */
    abstract val allowTypeParametersInVisitorMethods: Boolean

    /**
     * Allows to customize the default element to visit if the method for visiting this [element] is not overridden.
     *
     * If returns `null`, methods for this element will not be overridden in this visitor class (except the root element).
     */
    open fun parentInVisitor(element: Element): Element? = element.parentInVisitor

    /**
     * Prints a single visitor method declaration, without body.
     */
    context(ImportCollector)
    protected fun SmartPrinter.printVisitMethodDeclaration(
        element: Element,
        additionalParameters: List<FunctionParameter>,
        returnType: TypeRef,
        modality: Modality? = null,
        override: Boolean = false,
    ) {
        val visitorParameterType = ElementRef(
            element,
            element.params.associateWith { if (allowTypeParametersInVisitorMethods) it else TypeRef.Star }
        )
        printFunctionDeclaration(
            name = element.visitFunctionName,
            parameters = listOf(FunctionParameter(element.visitorParameterName, visitorParameterType)) + additionalParameters,
            returnType = returnType,
            typeParameters = if (allowTypeParametersInVisitorMethods) {
                element.params
            } else {
                emptyList()
            },
            modality = modality,
            override = override,
        )
    }

    context(ImportCollector)
    protected fun printMethodDeclarationForElement(element: Element, modality: Modality? = null, override: Boolean) {
        val parentVisitorType = this@AbstractVisitorPrinter.visitorSuperType
        val resultType = parentVisitorType?.args?.get(PositionTypeParameterRef(0)) ?: visitorTypeParameters[0]
        val dataType = parentVisitorType?.args?.get(PositionTypeParameterRef(1)) ?: visitorTypeParameters[1]
        printer.run {
            println()
            printVisitMethodDeclaration(
                element,
                additionalParameters = listOf(FunctionParameter("data", dataType)),
                returnType = resultType,
                modality = modality,
                override = override
            )
        }
    }

    context(ImportCollector)
    protected open fun printMethodsForElement(element: Element) {
        printer.run {
            val parentInVisitor = parentInVisitor(element)
            if (parentInVisitor == null && !element.isRootElement) return
            printMethodDeclarationForElement(
                element,
                modality = when {
                    visitorSuperType == null && parentInVisitor == null && visitorType.kind == TypeKind.Class -> Modality.ABSTRACT
                    visitorSuperType == null && parentInVisitor != null && visitorType.kind == TypeKind.Class -> Modality.OPEN
                    else -> null
                },
                override = parentInVisitor != null && visitorSuperType != null,
            )
            if (parentInVisitor != null) {
                print(" = ", parentInVisitor.visitFunctionName, "(", element.visitorParameterName, ", data)")
            }
            println()
        }
    }

    context(ImportCollector)
    protected open fun SmartPrinter.printAdditionalMethods() {}

    context(ImportCollector)
    fun printVisitor(elements: List<Element>) {
        val visitorType = this.visitorType
        printer.run {
            when (visitorType.kind) {
                TypeKind.Interface -> print("interface ")
                TypeKind.Class -> print("abstract class ")
            }
            print(visitorType.simpleName, visitorTypeParameters.typeParameters())
            visitorSuperType?.let {
                print(" : ", it.render(), it.inheritanceClauseParenthesis())
            }
            print(visitorTypeParameters.multipleUpperBoundsList())
            println(" {")
            withIndent {
                printAdditionalMethods()
                for (element in elements) {
                    if (element.isRootElement && visitSuperTypeByDefault) continue
                    if (visitSuperTypeByDefault && parentInVisitor(element) == null) continue
                    printMethodsForElement(element)
                }
            }
            println("}")
        }
    }
}