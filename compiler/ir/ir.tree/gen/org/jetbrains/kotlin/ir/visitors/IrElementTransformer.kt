/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/ir.tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.ir.visitors

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*

interface IrElementTransformer<in D> : IrElementVisitor<IrElement, D> {

    override fun visitElement(element: IrElement, data: D): IrElement {
        element.transformChildren(this, data)
        return element
    }

    override fun visitDeclaration(declaration: IrDeclarationBase, data: D): IrStatement {
        declaration.transformChildren(this, data)
        return declaration
    }

    override fun visitValueParameter(declaration: IrValueParameter, data: D): IrStatement =
        visitDeclaration(declaration, data)

    override fun visitClass(declaration: IrClass, data: D): IrStatement =
        visitDeclaration(declaration, data)

    override fun visitAnonymousInitializer(declaration: IrAnonymousInitializer, data: D): IrStatement =
        visitDeclaration(declaration, data)

    override fun visitTypeParameter(declaration: IrTypeParameter, data: D): IrStatement =
        visitDeclaration(declaration, data)

    override fun visitFunction(declaration: IrFunction, data: D): IrStatement =
        visitDeclaration(declaration, data)

    override fun visitConstructor(declaration: IrConstructor, data: D): IrStatement =
        visitFunction(declaration, data)

    override fun visitEnumEntry(declaration: IrEnumEntry, data: D): IrStatement =
        visitDeclaration(declaration, data)

    override fun visitErrorDeclaration(declaration: IrErrorDeclaration, data: D): IrStatement =
        visitDeclaration(declaration, data)

    override fun visitField(declaration: IrField, data: D): IrStatement =
        visitDeclaration(declaration, data)

    override fun visitLocalDelegatedProperty(declaration: IrLocalDelegatedProperty, data: D): IrStatement =
        visitDeclaration(declaration, data)

    override fun visitModuleFragment(declaration: IrModuleFragment, data: D): IrModuleFragment {
        declaration.transformChildren(this, data)
        return declaration
    }

    override fun visitProperty(declaration: IrProperty, data: D): IrStatement =
        visitDeclaration(declaration, data)

    override fun visitScript(declaration: IrScript, data: D): IrStatement =
        visitDeclaration(declaration, data)

    override fun visitSimpleFunction(declaration: IrSimpleFunction, data: D): IrStatement =
        visitFunction(declaration, data)

    override fun visitTypeAlias(declaration: IrTypeAlias, data: D): IrStatement =
        visitDeclaration(declaration, data)

    override fun visitVariable(declaration: IrVariable, data: D): IrStatement =
        visitDeclaration(declaration, data)

    override fun visitPackageFragment(declaration: IrPackageFragment, data: D): IrElement =
        visitElement(declaration, data)

    override fun visitExternalPackageFragment(declaration: IrExternalPackageFragment, data: D): IrExternalPackageFragment {
        declaration.transformChildren(this, data)
        return declaration
    }

    override fun visitFile(declaration: IrFile, data: D): IrFile {
        declaration.transformChildren(this, data)
        return declaration
    }

    override fun visitExpression(expression: IrExpression, data: D): IrExpression {
        expression.transformChildren(this, data)
        return expression
    }

    override fun visitBody(body: IrBody, data: D): IrBody {
        body.transformChildren(this, data)
        return body
    }

    override fun visitExpressionBody(body: IrExpressionBody, data: D): IrBody =
        visitBody(body, data)

    override fun visitBlockBody(body: IrBlockBody, data: D): IrBody =
        visitBody(body, data)

    override fun visitDeclarationReference(expression: IrDeclarationReference, data: D): IrExpression =
        visitExpression(expression, data)

    override fun visitMemberAccess(expression: IrMemberAccessExpression<*>, data: D): IrElement =
        visitDeclarationReference(expression, data)

    override fun visitFunctionAccess(expression: IrFunctionAccessExpression, data: D): IrElement =
        visitMemberAccess(expression, data)

    override fun visitConstructorCall(expression: IrConstructorCall, data: D): IrElement =
        visitFunctionAccess(expression, data)

    override fun visitSingletonReference(expression: IrGetSingletonValue, data: D): IrExpression =
        visitDeclarationReference(expression, data)

    override fun visitGetObjectValue(expression: IrGetObjectValue, data: D): IrExpression =
        visitSingletonReference(expression, data)

    override fun visitGetEnumValue(expression: IrGetEnumValue, data: D): IrExpression =
        visitSingletonReference(expression, data)

    override fun visitRawFunctionReference(expression: IrRawFunctionReference, data: D): IrExpression =
        visitDeclarationReference(expression, data)

    override fun visitContainerExpression(expression: IrContainerExpression, data: D): IrExpression =
        visitExpression(expression, data)

    override fun visitBlock(expression: IrBlock, data: D): IrExpression =
        visitContainerExpression(expression, data)

    override fun visitComposite(expression: IrComposite, data: D): IrExpression =
        visitContainerExpression(expression, data)

    override fun visitSyntheticBody(body: IrSyntheticBody, data: D): IrBody =
        visitBody(body, data)

    override fun visitBreakContinue(jump: IrBreakContinue, data: D): IrExpression =
        visitExpression(jump, data)

    override fun visitBreak(jump: IrBreak, data: D): IrExpression =
        visitBreakContinue(jump, data)

    override fun visitContinue(jump: IrContinue, data: D): IrExpression =
        visitBreakContinue(jump, data)

    override fun visitCall(expression: IrCall, data: D): IrElement =
        visitFunctionAccess(expression, data)

    override fun visitCallableReference(expression: IrCallableReference<*>, data: D): IrElement =
        visitMemberAccess(expression, data)

    override fun visitFunctionReference(expression: IrFunctionReference, data: D): IrElement =
        visitCallableReference(expression, data)

    override fun visitPropertyReference(expression: IrPropertyReference, data: D): IrElement =
        visitCallableReference(expression, data)

    override fun visitLocalDelegatedPropertyReference(expression: IrLocalDelegatedPropertyReference, data: D): IrElement =
        visitCallableReference(expression, data)

    override fun visitClassReference(expression: IrClassReference, data: D): IrExpression =
        visitDeclarationReference(expression, data)

    override fun visitConst(expression: IrConst<*>, data: D): IrExpression =
        visitExpression(expression, data)

    override fun visitConstantValue(expression: IrConstantValue, data: D): IrConstantValue {
        expression.transformChildren(this, data)
        return expression
    }

    override fun visitConstantPrimitive(expression: IrConstantPrimitive, data: D): IrConstantValue =
        visitConstantValue(expression, data)

    override fun visitConstantObject(expression: IrConstantObject, data: D): IrConstantValue =
        visitConstantValue(expression, data)

    override fun visitConstantArray(expression: IrConstantArray, data: D): IrConstantValue =
        visitConstantValue(expression, data)

    override fun visitDelegatingConstructorCall(expression: IrDelegatingConstructorCall, data: D): IrElement =
        visitFunctionAccess(expression, data)

    override fun visitDynamicExpression(expression: IrDynamicExpression, data: D): IrExpression =
        visitExpression(expression, data)

    override fun visitDynamicOperatorExpression(expression: IrDynamicOperatorExpression, data: D): IrExpression =
        visitDynamicExpression(expression, data)

    override fun visitDynamicMemberExpression(expression: IrDynamicMemberExpression, data: D): IrExpression =
        visitDynamicExpression(expression, data)

    override fun visitEnumConstructorCall(expression: IrEnumConstructorCall, data: D): IrElement =
        visitFunctionAccess(expression, data)

    override fun visitErrorExpression(expression: IrErrorExpression, data: D): IrExpression =
        visitExpression(expression, data)

    override fun visitErrorCallExpression(expression: IrErrorCallExpression, data: D): IrExpression =
        visitErrorExpression(expression, data)

    override fun visitFieldAccess(expression: IrFieldAccessExpression, data: D): IrExpression =
        visitDeclarationReference(expression, data)

    override fun visitGetField(expression: IrGetField, data: D): IrExpression =
        visitFieldAccess(expression, data)

    override fun visitSetField(expression: IrSetField, data: D): IrExpression =
        visitFieldAccess(expression, data)

    override fun visitFunctionExpression(expression: IrFunctionExpression, data: D): IrElement =
        visitExpression(expression, data)

    override fun visitGetClass(expression: IrGetClass, data: D): IrExpression =
        visitExpression(expression, data)

    override fun visitInstanceInitializerCall(expression: IrInstanceInitializerCall, data: D): IrExpression =
        visitExpression(expression, data)

    override fun visitLoop(loop: IrLoop, data: D): IrExpression =
        visitExpression(loop, data)

    override fun visitWhileLoop(loop: IrWhileLoop, data: D): IrExpression =
        visitLoop(loop, data)

    override fun visitDoWhileLoop(loop: IrDoWhileLoop, data: D): IrExpression =
        visitLoop(loop, data)

    override fun visitReturn(expression: IrReturn, data: D): IrExpression =
        visitExpression(expression, data)

    override fun visitStringConcatenation(expression: IrStringConcatenation, data: D): IrExpression =
        visitExpression(expression, data)

    override fun visitSuspensionPoint(expression: IrSuspensionPoint, data: D): IrExpression =
        visitExpression(expression, data)

    override fun visitSuspendableExpression(expression: IrSuspendableExpression, data: D): IrExpression =
        visitExpression(expression, data)

    override fun visitThrow(expression: IrThrow, data: D): IrExpression =
        visitExpression(expression, data)

    override fun visitTry(aTry: IrTry, data: D): IrExpression =
        visitExpression(aTry, data)

    override fun visitCatch(aCatch: IrCatch, data: D): IrCatch {
        aCatch.transformChildren(this, data)
        return aCatch
    }

    override fun visitTypeOperator(expression: IrTypeOperatorCall, data: D): IrExpression =
        visitExpression(expression, data)

    override fun visitValueAccess(expression: IrValueAccessExpression, data: D): IrExpression =
        visitDeclarationReference(expression, data)

    override fun visitGetValue(expression: IrGetValue, data: D): IrExpression =
        visitValueAccess(expression, data)

    override fun visitSetValue(expression: IrSetValue, data: D): IrExpression =
        visitValueAccess(expression, data)

    override fun visitVararg(expression: IrVararg, data: D): IrExpression =
        visitExpression(expression, data)

    override fun visitSpreadElement(spread: IrSpreadElement, data: D): IrSpreadElement {
        spread.transformChildren(this, data)
        return spread
    }

    override fun visitWhen(expression: IrWhen, data: D): IrExpression =
        visitExpression(expression, data)

    override fun visitBranch(branch: IrBranch, data: D): IrBranch {
        branch.transformChildren(this, data)
        return branch
    }

    override fun visitElseBranch(branch: IrElseBranch, data: D): IrElseBranch {
        branch.transformChildren(this, data)
        return branch
    }
}
