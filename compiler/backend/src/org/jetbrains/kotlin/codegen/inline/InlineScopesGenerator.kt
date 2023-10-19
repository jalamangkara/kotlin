/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.codegen.inline

import org.jetbrains.kotlin.load.java.JvmAbi
import org.jetbrains.org.objectweb.asm.Label
import org.jetbrains.org.objectweb.asm.tree.LabelNode
import org.jetbrains.org.objectweb.asm.tree.LineNumberNode
import org.jetbrains.org.objectweb.asm.tree.LocalVariableNode
import org.jetbrains.org.objectweb.asm.tree.MethodNode

class InlineScopesGenerator {
    var inlinedScopes = 0
    var currentCallSiteLineNumber = 0

    private data class ScopeInfo(val variable: LocalVariableNode, val scopeNumber: Int, val inlineNesting: Int)

    private data class MarkerVariableInfo(val callSiteLineNumber: Int, val surroundingScopeNumber: Int?, val inlineNesting: Int)

    private abstract inner class VariableRenamer {
        val inlineScopesStack = mutableListOf<ScopeInfo>()

        abstract fun inlineNesting(): Int

        abstract fun visitMarkerVariable(
            variable: LocalVariableNode,
            scopeNumber: Int,
            inlineNesting: Int
        ): MarkerVariableInfo

        abstract fun shouldPostponeAddingAScopeNumber(variable: LocalVariableNode, inlineNesting: Int): Boolean

        abstract fun shouldSkipVariable(variable: LocalVariableNode): Boolean

        fun renameVariables(node: MethodNode): Int {
            val localVariables = node.localVariables ?: return 0
            val labelToIndex = node.getLabelToIndexMap()

            fun LocalVariableNode.contains(other: LocalVariableNode): Boolean {
                val startIndex = labelToIndex[start.label] ?: return false
                val endIndex = labelToIndex[end.label] ?: return false
                val otherStartIndex = labelToIndex[other.start.label] ?: return false
                val otherEndIndex = labelToIndex[other.end.label] ?: return false
                return startIndex < otherStartIndex && endIndex >= otherEndIndex
            }

            // The scope number 0 belongs to the top frame
            var currentInlineScopeNumber = 0
            var currentInlineNesting = inlineNesting()

            // Inline function and lambda parameters are introduced before the corresponding inline marker variable,
            // so we need to keep track of them to assign the correct scope number later.
            val variablesWithNotMatchingDepth = mutableListOf<LocalVariableNode>()
            var seenInlineScopesNumber = 0

            val sortedVariables = localVariables.sortedBy { labelToIndex[it.start.label] }
            for (variable in sortedVariables) {
                while (inlineScopesStack.isNotEmpty() && !inlineScopesStack.last().variable.contains(variable)) {
                    inlineScopesStack.removeLast()
                }

                val name = variable.name
                if (inlineScopesStack.isNotEmpty()) {
                    val info = inlineScopesStack.last()
                    currentInlineScopeNumber = info.scopeNumber
                    currentInlineNesting = info.inlineNesting
                } else if (shouldSkipVariable(variable)) {
                    continue
                }

                if (isFakeLocalVariableForInline(name)) {
                    seenInlineScopesNumber += 1
                    currentInlineScopeNumber = seenInlineScopesNumber

                    val (callSiteLineNumber, surroundingScopeNumber, inlineNesting) = visitMarkerVariable(
                        variable,
                        currentInlineScopeNumber,
                        currentInlineNesting
                    )

                    inlineScopesStack += ScopeInfo(variable, currentInlineScopeNumber, inlineNesting)

                    variable.name = computeNewVariableName(
                        name,
                        currentInlineScopeNumber + inlinedScopes,
                        callSiteLineNumber,
                        surroundingScopeNumber
                    )
                    variablesWithNotMatchingDepth.forEach {
                        it.name = computeNewVariableName(it.name, currentInlineScopeNumber + inlinedScopes)
                    }
                    variablesWithNotMatchingDepth.clear()
                } else {
                    if (shouldPostponeAddingAScopeNumber(variable, currentInlineNesting)) {
                        variablesWithNotMatchingDepth.add(variable)
                    } else {
                        variable.name = computeNewVariableName(name, currentInlineScopeNumber + inlinedScopes)
                    }
                }
            }

            return seenInlineScopesNumber
        }
    }

    fun addInlineScopesInfo(node: MethodNode, isRegeneratingAnonymousObject: Boolean) {
        val localVariables = node.localVariables
        if (localVariables?.isEmpty() == true) {
            return
        }

        val markerVariablesWithoutScopeInfoNum = localVariables.count {
            isFakeLocalVariableForInline(it.name) && !it.name.contains(INLINE_SCOPE_NUMBER_SEPARATOR)
        }

        when {
            isRegeneratingAnonymousObject -> {
                if (markerVariablesWithoutScopeInfoNum > 0) {
                    addInlineScopesInfoFromIVSuffixesWhenRegeneratingAnonymousObject(node)
                }
            }
            // When inlining a function its marker variable won't contain any scope numbers yet.
            // But if there are more than one marker variable like this, it means that we
            // are inlining the code produced by the old compiler versions, where inline scopes
            // have not been introduced.
            markerVariablesWithoutScopeInfoNum == 1 ->
                addInlineScopesInfoFromScopeNumbers(node)
            else ->
                addInlineScopesInfoFromIVSuffixes(node)
        }
    }

    private fun addInlineScopesInfoFromScopeNumbers(node: MethodNode) {
        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        val renamer = object : VariableRenamer() {
            // Here inline nesting means old scope number of current marker variable.
            // We initialise it with -1 because we haven't assigned any scope numbers yet.
            override fun inlineNesting(): Int = -1

            override fun shouldSkipVariable(variable: LocalVariableNode): Boolean = false

            override fun visitMarkerVariable(
                variable: LocalVariableNode,
                scopeNumber: Int,
                oldScopeNumberOfCurrentMarkerVariable: Int
            ): MarkerVariableInfo {
                val name = variable.name
                val info = name.getInlineScopeInfo()
                val callSiteLineNumber =
                    if (scopeNumber == 1) {
                        currentCallSiteLineNumber
                    } else {
                        info?.callSiteLineNumber ?: 0
                    }

                if (name.isInlineLambdaMarkerVariableName) {
                    val surroundingScopeNumber = info?.surroundingScopeNumber
                    val newSurroundingScopeNumber =
                        when {
                            // The first encountered inline scope belongs to the lambda, which means
                            // that its surrounding scope is the function where the lambda is being inlined to.
                            scopeNumber == 1 -> 0
                            // Every lambda that is already inlined must have a surrounding scope number.
                            // If it doesn't, then it means that we are inlining the code compiled by
                            // the older versions of the Kotlin compiler, where surrounding scope numbers
                            // haven't been introduced yet.
                            surroundingScopeNumber != null -> surroundingScopeNumber + inlinedScopes + 1
                            // This situation shouldn't happen, so add invalid info here
                            else -> -1
                        }
                    return MarkerVariableInfo(callSiteLineNumber, newSurroundingScopeNumber, info?.scopeNumber ?: 0)
                }
                return MarkerVariableInfo(callSiteLineNumber, null, info?.scopeNumber ?: 0)
            }

            override fun shouldPostponeAddingAScopeNumber(
                variable: LocalVariableNode,
                oldScopeNumberOfCurrentMarkerVariable: Int
            ): Boolean {
                val scopeNumber = variable.name.getInlineScopeInfo()?.scopeNumber
                if (scopeNumber != null) {
                    return scopeNumber != oldScopeNumberOfCurrentMarkerVariable
                }
                return inlineScopesStack.isEmpty()
            }
        }

        inlinedScopes += renamer.renameVariables(node)
    }

    private fun addInlineScopesInfoFromIVSuffixes(node: MethodNode) {
        val labelToLineNumber = node.getLabelToLineNumberMap()

        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        val renamer = object : VariableRenamer() {
            // Here inline nesting means depth in $iv suffixes.
            // We initialise it with -1 so that the first marker variable
            // that we encounter has depth equal to 0. The first marker variable
            // represents the function being inlined in this case.
            override fun inlineNesting(): Int = -1

            override fun shouldSkipVariable(variable: LocalVariableNode): Boolean = false

            override fun visitMarkerVariable(variable: LocalVariableNode, scopeNumber: Int, ivDepth: Int): MarkerVariableInfo {
                val name = variable.name
                val currentIVDepth =
                    if (name.isInlineLambdaMarkerVariableName) {
                        getInlineDepth(name)
                    } else {
                        ivDepth + 1
                    }

                val callSiteLineNumber =
                    if (scopeNumber == 1) {
                        currentCallSiteLineNumber
                    } else {
                        // When inlining from the code compiled by the old compiler versions,
                        // the marker variable will not contain the call site line number.
                        // In this case we will take the line number of the variable start offset
                        // as the call site line number.
                        labelToLineNumber[variable.start.label] ?: 0
                    }

                if (name.isInlineLambdaMarkerVariableName) {
                    val newSurroundingScopeNumber =
                        when {
                            scopeNumber == 1 -> 0
                            else -> {
                                val surroundingScopeNumber =
                                    inlineScopesStack.findSurroundingScopeNumber(currentIVDepth)
                                        ?: inlineScopesStack.firstOrNull()?.scopeNumber
                                surroundingScopeNumber?.plus(inlinedScopes) ?: 0
                            }
                        }
                    return MarkerVariableInfo(callSiteLineNumber, newSurroundingScopeNumber, currentIVDepth)
                }
                return MarkerVariableInfo(callSiteLineNumber, null, currentIVDepth)
            }

            override fun shouldPostponeAddingAScopeNumber(variable: LocalVariableNode, ivDepth: Int): Boolean =
                inlineScopesStack.isEmpty() || getInlineDepth(variable.name) != ivDepth
        }

        inlinedScopes += renamer.renameVariables(node)
    }

    private fun addInlineScopesInfoFromIVSuffixesWhenRegeneratingAnonymousObject(node: MethodNode) {
        val labelToLineNumber = node.getLabelToLineNumberMap()

        // This renamer is slightly different from the one we used when computing inline scopes from the
        // $iv suffixes. Here no function is being inlined, so the base depth in $iv suffixes is equal to 0.
        // When we meet the first marker variable, it should have its depth equal to 1. Apart from that,
        // when calculating call site line numbers, we always pick the line number of the marker variable
        // start offset and not rely on the `currentCallSiteLineNumber` field. Also, when computing surrounding
        // scope numbers we assign surrounding scope 0 (that represents the top frame) to inline lambda
        // marker variables that don't have a surrounding scope.
        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        val renamer = object : VariableRenamer() {
            // Here inline nesting means depth in $iv suffixes.
            // On contrary with the situation when we are inlining a function,
            // here we won't meet a marker variable that represents the method node.
            // When we meet the first marker variable, it should have depth equal to 1.
            override fun inlineNesting(): Int = 0

            override fun shouldSkipVariable(variable: LocalVariableNode): Boolean =
                !isFakeLocalVariableForInline(variable.name) && !variable.name.contains(INLINE_FUN_VAR_SUFFIX)

            override fun visitMarkerVariable(variable: LocalVariableNode, scopeNumber: Int, ivDepth: Int): MarkerVariableInfo {
                val name = variable.name
                val currentIVDepth =
                    if (name.isInlineLambdaMarkerVariableName) {
                        getInlineDepth(name)
                    } else {
                        ivDepth + 1
                    }

                val callSiteLineNumber = labelToLineNumber[variable.start.label] ?: 0
                if (name.isInlineLambdaMarkerVariableName) {
                    val newSurroundingScopeNumber =
                        when {
                            scopeNumber == 1 -> 0
                            else -> {
                                val surroundingScopeNumber = inlineScopesStack.findSurroundingScopeNumber(currentIVDepth)
                                surroundingScopeNumber?.plus(inlinedScopes) ?: 0
                            }
                        }
                    return MarkerVariableInfo(callSiteLineNumber, newSurroundingScopeNumber, currentIVDepth)
                }
                return MarkerVariableInfo(callSiteLineNumber, null, currentIVDepth)
            }

            override fun shouldPostponeAddingAScopeNumber(variable: LocalVariableNode, ivDepth: Int): Boolean =
                inlineScopesStack.isEmpty() || getInlineDepth(variable.name) != ivDepth
        }

        renamer.renameVariables(node)
    }

    private fun List<ScopeInfo>.findSurroundingScopeNumber(currentIVDepth: Int): Int? {
        val surroundingScopeInfo = if (currentIVDepth != 0) {
            lastOrNull { it.inlineNesting == currentIVDepth }
        } else {
            lastOrNull { it.variable.name.isInlineLambdaMarkerVariableName }
        }
        return surroundingScopeInfo?.scopeNumber
    }

    private fun computeNewVariableName(
        name: String,
        scopeNumber: Int,
        callSiteLineNumber: Int? = null,
        surroundingScopeNumber: Int? = null
    ): String {
        val prefix = name.replace(INLINE_FUN_VAR_SUFFIX, "").dropInlineScopeInfo()
        return buildString {
            append(prefix)
            append(INLINE_SCOPE_NUMBER_SEPARATOR)
            append(scopeNumber)

            if (callSiteLineNumber != null) {
                append(INLINE_SCOPE_NUMBER_SEPARATOR)
                append(callSiteLineNumber)
            }

            if (surroundingScopeNumber != null) {
                append(INLINE_SCOPE_NUMBER_SEPARATOR)
                append(surroundingScopeNumber)
            }
        }
    }
}

fun updateCallSiteLineNumber(name: String, lineNumberMapping: Map<Int, Int>): String =
    updateCallSiteLineNumber(name) { lineNumberMapping[it] ?: it }

fun updateCallSiteLineNumber(name: String, newLineNumber: Int): String =
    updateCallSiteLineNumber(name) { newLineNumber }

private fun updateCallSiteLineNumber(name: String, calculate: (Int) -> Int): String {
    val (scopeNumber, callSiteLineNumber, surroundingScopeNumber) = name.getInlineScopeInfo() ?: return name
    if (callSiteLineNumber == null) {
        return name
    }

    val newLineNumber = calculate(callSiteLineNumber)
    if (newLineNumber == callSiteLineNumber) {
        return name
    }

    val newName = name
        .dropInlineScopeInfo()
        .addScopeInfo(scopeNumber)
        .addScopeInfo(newLineNumber)

    if (surroundingScopeNumber == null) {
        return newName
    }
    return newName.addScopeInfo(surroundingScopeNumber)
}

internal fun MethodNode.getLabelToIndexMap(): Map<Label, Int> =
    instructions.filterIsInstance<LabelNode>()
        .withIndex()
        .associate { (index, labelNode) ->
            labelNode.label to index
        }

private fun MethodNode.getLabelToLineNumberMap(): Map<Label, Int> {
    val result = mutableMapOf<Label, Int>()
    var currentLineNumber = 0
    for (insn in instructions) {
        if (insn is LineNumberNode) {
            currentLineNumber = insn.line
        } else if (insn is LabelNode) {
            result[insn.label] = currentLineNumber
        }
    }

    return result
}

fun String.addScopeInfo(number: Int): String =
    "$this$INLINE_SCOPE_NUMBER_SEPARATOR$number"

private fun getInlineDepth(variableName: String): Int {
    var endIndex = variableName.length
    var depth = 0

    val suffixLen = INLINE_FUN_VAR_SUFFIX.length
    while (endIndex >= suffixLen) {
        if (variableName.substring(endIndex - suffixLen, endIndex) != INLINE_FUN_VAR_SUFFIX) {
            break
        }

        depth++
        endIndex -= suffixLen
    }

    return depth
}

private val String.isInlineLambdaMarkerVariableName: Boolean
    get() = startsWith(JvmAbi.LOCAL_VARIABLE_NAME_PREFIX_INLINE_ARGUMENT)
