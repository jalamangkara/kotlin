/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/fir/tree/tree-generator/Readme.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.fir.visitors

import org.jetbrains.kotlin.fir.*
import org.jetbrains.kotlin.fir.contracts.*
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.diagnostics.FirDiagnosticHolder
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.references.*
import org.jetbrains.kotlin.fir.types.*

abstract class FirTransformer<in D> : FirVisitor<FirElement, D>() {

    abstract fun <E : FirElement> transformElement(element: E, data: D): E

    final override fun visitElement(element: FirElement, data: D): FirElement {
        return transformElement(element, data)
    }

    open fun transformAnnotationContainer(annotationContainer: FirAnnotationContainer, data: D): FirAnnotationContainer {
        return transformElement(annotationContainer, data)
    }

    final override fun visitAnnotationContainer(annotationContainer: FirAnnotationContainer, data: D): FirAnnotationContainer {
        return transformAnnotationContainer(annotationContainer, data)
    }

    open fun transformTypeRef(typeRef: FirTypeRef, data: D): FirTypeRef {
        return transformElement(typeRef, data)
    }

    final override fun visitTypeRef(typeRef: FirTypeRef, data: D): FirTypeRef {
        return transformTypeRef(typeRef, data)
    }

    open fun transformReference(reference: FirReference, data: D): FirReference {
        return transformElement(reference, data)
    }

    final override fun visitReference(reference: FirReference, data: D): FirReference {
        return transformReference(reference, data)
    }

    open fun transformLabel(label: FirLabel, data: D): FirLabel {
        return transformElement(label, data)
    }

    final override fun visitLabel(label: FirLabel, data: D): FirLabel {
        return transformLabel(label, data)
    }

    open fun transformResolvable(resolvable: FirResolvable, data: D): FirResolvable {
        return transformElement(resolvable, data)
    }

    final override fun visitResolvable(resolvable: FirResolvable, data: D): FirResolvable {
        return transformResolvable(resolvable, data)
    }

    open fun transformTargetElement(targetElement: FirTargetElement, data: D): FirTargetElement {
        return transformElement(targetElement, data)
    }

    final override fun visitTargetElement(targetElement: FirTargetElement, data: D): FirTargetElement {
        return transformTargetElement(targetElement, data)
    }

    open fun transformDeclarationStatus(declarationStatus: FirDeclarationStatus, data: D): FirDeclarationStatus {
        return transformElement(declarationStatus, data)
    }

    final override fun visitDeclarationStatus(declarationStatus: FirDeclarationStatus, data: D): FirDeclarationStatus {
        return transformDeclarationStatus(declarationStatus, data)
    }

    open fun transformResolvedDeclarationStatus(resolvedDeclarationStatus: FirResolvedDeclarationStatus, data: D): FirDeclarationStatus {
        return transformElement(resolvedDeclarationStatus, data)
    }

    final override fun visitResolvedDeclarationStatus(resolvedDeclarationStatus: FirResolvedDeclarationStatus, data: D): FirDeclarationStatus {
        return transformResolvedDeclarationStatus(resolvedDeclarationStatus, data)
    }

    open fun transformControlFlowGraphOwner(controlFlowGraphOwner: FirControlFlowGraphOwner, data: D): FirControlFlowGraphOwner {
        return transformElement(controlFlowGraphOwner, data)
    }

    final override fun visitControlFlowGraphOwner(controlFlowGraphOwner: FirControlFlowGraphOwner, data: D): FirControlFlowGraphOwner {
        return transformControlFlowGraphOwner(controlFlowGraphOwner, data)
    }

    open fun transformStatement(statement: FirStatement, data: D): FirStatement {
        return transformElement(statement, data)
    }

    final override fun visitStatement(statement: FirStatement, data: D): FirStatement {
        return transformStatement(statement, data)
    }

    open fun transformExpression(expression: FirExpression, data: D): FirStatement {
        return transformElement(expression, data)
    }

    final override fun visitExpression(expression: FirExpression, data: D): FirStatement {
        return transformExpression(expression, data)
    }

    open fun transformLazyExpression(lazyExpression: FirLazyExpression, data: D): FirStatement {
        return transformElement(lazyExpression, data)
    }

    final override fun visitLazyExpression(lazyExpression: FirLazyExpression, data: D): FirStatement {
        return transformLazyExpression(lazyExpression, data)
    }

    open fun transformContextReceiver(contextReceiver: FirContextReceiver, data: D): FirContextReceiver {
        return transformElement(contextReceiver, data)
    }

    final override fun visitContextReceiver(contextReceiver: FirContextReceiver, data: D): FirContextReceiver {
        return transformContextReceiver(contextReceiver, data)
    }

    open fun transformElementWithResolveState(elementWithResolveState: FirElementWithResolveState, data: D): FirElementWithResolveState {
        return transformElement(elementWithResolveState, data)
    }

    final override fun visitElementWithResolveState(elementWithResolveState: FirElementWithResolveState, data: D): FirElementWithResolveState {
        return transformElementWithResolveState(elementWithResolveState, data)
    }

    open fun transformFileAnnotationsContainer(fileAnnotationsContainer: FirFileAnnotationsContainer, data: D): FirFileAnnotationsContainer {
        return transformElement(fileAnnotationsContainer, data)
    }

    final override fun visitFileAnnotationsContainer(fileAnnotationsContainer: FirFileAnnotationsContainer, data: D): FirFileAnnotationsContainer {
        return transformFileAnnotationsContainer(fileAnnotationsContainer, data)
    }

    open fun transformDeclaration(declaration: FirDeclaration, data: D): FirDeclaration {
        return transformElement(declaration, data)
    }

    final override fun visitDeclaration(declaration: FirDeclaration, data: D): FirDeclaration {
        return transformDeclaration(declaration, data)
    }

    open fun transformTypeParameterRefsOwner(typeParameterRefsOwner: FirTypeParameterRefsOwner, data: D): FirTypeParameterRefsOwner {
        return transformElement(typeParameterRefsOwner, data)
    }

    final override fun visitTypeParameterRefsOwner(typeParameterRefsOwner: FirTypeParameterRefsOwner, data: D): FirTypeParameterRefsOwner {
        return transformTypeParameterRefsOwner(typeParameterRefsOwner, data)
    }

    open fun transformTypeParametersOwner(typeParametersOwner: FirTypeParametersOwner, data: D): FirTypeParametersOwner {
        return transformElement(typeParametersOwner, data)
    }

    final override fun visitTypeParametersOwner(typeParametersOwner: FirTypeParametersOwner, data: D): FirTypeParametersOwner {
        return transformTypeParametersOwner(typeParametersOwner, data)
    }

    open fun transformMemberDeclaration(memberDeclaration: FirMemberDeclaration, data: D): FirMemberDeclaration {
        return transformElement(memberDeclaration, data)
    }

    final override fun visitMemberDeclaration(memberDeclaration: FirMemberDeclaration, data: D): FirMemberDeclaration {
        return transformMemberDeclaration(memberDeclaration, data)
    }

    open fun transformAnonymousInitializer(anonymousInitializer: FirAnonymousInitializer, data: D): FirAnonymousInitializer {
        return transformElement(anonymousInitializer, data)
    }

    final override fun visitAnonymousInitializer(anonymousInitializer: FirAnonymousInitializer, data: D): FirAnonymousInitializer {
        return transformAnonymousInitializer(anonymousInitializer, data)
    }

    open fun transformCallableDeclaration(callableDeclaration: FirCallableDeclaration, data: D): FirCallableDeclaration {
        return transformElement(callableDeclaration, data)
    }

    final override fun visitCallableDeclaration(callableDeclaration: FirCallableDeclaration, data: D): FirCallableDeclaration {
        return transformCallableDeclaration(callableDeclaration, data)
    }

    open fun transformTypeParameterRef(typeParameterRef: FirTypeParameterRef, data: D): FirTypeParameterRef {
        return transformElement(typeParameterRef, data)
    }

    final override fun visitTypeParameterRef(typeParameterRef: FirTypeParameterRef, data: D): FirTypeParameterRef {
        return transformTypeParameterRef(typeParameterRef, data)
    }

    open fun transformTypeParameter(typeParameter: FirTypeParameter, data: D): FirTypeParameterRef {
        return transformElement(typeParameter, data)
    }

    final override fun visitTypeParameter(typeParameter: FirTypeParameter, data: D): FirTypeParameterRef {
        return transformTypeParameter(typeParameter, data)
    }

    open fun transformConstructedClassTypeParameterRef(constructedClassTypeParameterRef: FirConstructedClassTypeParameterRef, data: D): FirTypeParameterRef {
        return transformElement(constructedClassTypeParameterRef, data)
    }

    final override fun visitConstructedClassTypeParameterRef(constructedClassTypeParameterRef: FirConstructedClassTypeParameterRef, data: D): FirTypeParameterRef {
        return transformConstructedClassTypeParameterRef(constructedClassTypeParameterRef, data)
    }

    open fun transformOuterClassTypeParameterRef(outerClassTypeParameterRef: FirOuterClassTypeParameterRef, data: D): FirTypeParameterRef {
        return transformElement(outerClassTypeParameterRef, data)
    }

    final override fun visitOuterClassTypeParameterRef(outerClassTypeParameterRef: FirOuterClassTypeParameterRef, data: D): FirTypeParameterRef {
        return transformOuterClassTypeParameterRef(outerClassTypeParameterRef, data)
    }

    open fun transformVariable(variable: FirVariable, data: D): FirStatement {
        return transformElement(variable, data)
    }

    final override fun visitVariable(variable: FirVariable, data: D): FirStatement {
        return transformVariable(variable, data)
    }

    open fun transformValueParameter(valueParameter: FirValueParameter, data: D): FirStatement {
        return transformElement(valueParameter, data)
    }

    final override fun visitValueParameter(valueParameter: FirValueParameter, data: D): FirStatement {
        return transformValueParameter(valueParameter, data)
    }

    open fun transformReceiverParameter(receiverParameter: FirReceiverParameter, data: D): FirReceiverParameter {
        return transformElement(receiverParameter, data)
    }

    final override fun visitReceiverParameter(receiverParameter: FirReceiverParameter, data: D): FirReceiverParameter {
        return transformReceiverParameter(receiverParameter, data)
    }

    open fun transformProperty(property: FirProperty, data: D): FirStatement {
        return transformElement(property, data)
    }

    final override fun visitProperty(property: FirProperty, data: D): FirStatement {
        return transformProperty(property, data)
    }

    open fun transformField(field: FirField, data: D): FirStatement {
        return transformElement(field, data)
    }

    final override fun visitField(field: FirField, data: D): FirStatement {
        return transformField(field, data)
    }

    open fun transformEnumEntry(enumEntry: FirEnumEntry, data: D): FirStatement {
        return transformElement(enumEntry, data)
    }

    final override fun visitEnumEntry(enumEntry: FirEnumEntry, data: D): FirStatement {
        return transformEnumEntry(enumEntry, data)
    }

    open fun transformFunctionTypeParameter(functionTypeParameter: FirFunctionTypeParameter, data: D): FirFunctionTypeParameter {
        return transformElement(functionTypeParameter, data)
    }

    final override fun visitFunctionTypeParameter(functionTypeParameter: FirFunctionTypeParameter, data: D): FirFunctionTypeParameter {
        return transformFunctionTypeParameter(functionTypeParameter, data)
    }

    open fun transformClassLikeDeclaration(classLikeDeclaration: FirClassLikeDeclaration, data: D): FirStatement {
        return transformElement(classLikeDeclaration, data)
    }

    final override fun visitClassLikeDeclaration(classLikeDeclaration: FirClassLikeDeclaration, data: D): FirStatement {
        return transformClassLikeDeclaration(classLikeDeclaration, data)
    }

    open fun transformClass(klass: FirClass, data: D): FirStatement {
        return transformElement(klass, data)
    }

    final override fun visitClass(klass: FirClass, data: D): FirStatement {
        return transformClass(klass, data)
    }

    open fun transformRegularClass(regularClass: FirRegularClass, data: D): FirStatement {
        return transformElement(regularClass, data)
    }

    final override fun visitRegularClass(regularClass: FirRegularClass, data: D): FirStatement {
        return transformRegularClass(regularClass, data)
    }

    open fun transformTypeAlias(typeAlias: FirTypeAlias, data: D): FirStatement {
        return transformElement(typeAlias, data)
    }

    final override fun visitTypeAlias(typeAlias: FirTypeAlias, data: D): FirStatement {
        return transformTypeAlias(typeAlias, data)
    }

    open fun transformFunction(function: FirFunction, data: D): FirStatement {
        return transformElement(function, data)
    }

    final override fun visitFunction(function: FirFunction, data: D): FirStatement {
        return transformFunction(function, data)
    }

    open fun transformContractDescriptionOwner(contractDescriptionOwner: FirContractDescriptionOwner, data: D): FirContractDescriptionOwner {
        return transformElement(contractDescriptionOwner, data)
    }

    final override fun visitContractDescriptionOwner(contractDescriptionOwner: FirContractDescriptionOwner, data: D): FirContractDescriptionOwner {
        return transformContractDescriptionOwner(contractDescriptionOwner, data)
    }

    open fun transformSimpleFunction(simpleFunction: FirSimpleFunction, data: D): FirStatement {
        return transformElement(simpleFunction, data)
    }

    final override fun visitSimpleFunction(simpleFunction: FirSimpleFunction, data: D): FirStatement {
        return transformSimpleFunction(simpleFunction, data)
    }

    open fun transformPropertyAccessor(propertyAccessor: FirPropertyAccessor, data: D): FirStatement {
        return transformElement(propertyAccessor, data)
    }

    final override fun visitPropertyAccessor(propertyAccessor: FirPropertyAccessor, data: D): FirStatement {
        return transformPropertyAccessor(propertyAccessor, data)
    }

    open fun transformBackingField(backingField: FirBackingField, data: D): FirStatement {
        return transformElement(backingField, data)
    }

    final override fun visitBackingField(backingField: FirBackingField, data: D): FirStatement {
        return transformBackingField(backingField, data)
    }

    open fun transformConstructor(constructor: FirConstructor, data: D): FirStatement {
        return transformElement(constructor, data)
    }

    final override fun visitConstructor(constructor: FirConstructor, data: D): FirStatement {
        return transformConstructor(constructor, data)
    }

    open fun transformFile(file: FirFile, data: D): FirFile {
        return transformElement(file, data)
    }

    final override fun visitFile(file: FirFile, data: D): FirFile {
        return transformFile(file, data)
    }

    open fun transformScript(script: FirScript, data: D): FirScript {
        return transformElement(script, data)
    }

    final override fun visitScript(script: FirScript, data: D): FirScript {
        return transformScript(script, data)
    }

    open fun transformCodeFragment(codeFragment: FirCodeFragment, data: D): FirCodeFragment {
        return transformElement(codeFragment, data)
    }

    final override fun visitCodeFragment(codeFragment: FirCodeFragment, data: D): FirCodeFragment {
        return transformCodeFragment(codeFragment, data)
    }

    open fun transformPackageDirective(packageDirective: FirPackageDirective, data: D): FirPackageDirective {
        return transformElement(packageDirective, data)
    }

    final override fun visitPackageDirective(packageDirective: FirPackageDirective, data: D): FirPackageDirective {
        return transformPackageDirective(packageDirective, data)
    }

    open fun transformAnonymousFunction(anonymousFunction: FirAnonymousFunction, data: D): FirStatement {
        return transformElement(anonymousFunction, data)
    }

    final override fun visitAnonymousFunction(anonymousFunction: FirAnonymousFunction, data: D): FirStatement {
        return transformAnonymousFunction(anonymousFunction, data)
    }

    open fun transformAnonymousFunctionExpression(anonymousFunctionExpression: FirAnonymousFunctionExpression, data: D): FirStatement {
        return transformElement(anonymousFunctionExpression, data)
    }

    final override fun visitAnonymousFunctionExpression(anonymousFunctionExpression: FirAnonymousFunctionExpression, data: D): FirStatement {
        return transformAnonymousFunctionExpression(anonymousFunctionExpression, data)
    }

    open fun transformAnonymousObject(anonymousObject: FirAnonymousObject, data: D): FirStatement {
        return transformElement(anonymousObject, data)
    }

    final override fun visitAnonymousObject(anonymousObject: FirAnonymousObject, data: D): FirStatement {
        return transformAnonymousObject(anonymousObject, data)
    }

    open fun transformAnonymousObjectExpression(anonymousObjectExpression: FirAnonymousObjectExpression, data: D): FirStatement {
        return transformElement(anonymousObjectExpression, data)
    }

    final override fun visitAnonymousObjectExpression(anonymousObjectExpression: FirAnonymousObjectExpression, data: D): FirStatement {
        return transformAnonymousObjectExpression(anonymousObjectExpression, data)
    }

    open fun transformDiagnosticHolder(diagnosticHolder: FirDiagnosticHolder, data: D): FirDiagnosticHolder {
        return transformElement(diagnosticHolder, data)
    }

    final override fun visitDiagnosticHolder(diagnosticHolder: FirDiagnosticHolder, data: D): FirDiagnosticHolder {
        return transformDiagnosticHolder(diagnosticHolder, data)
    }

    open fun transformImport(import: FirImport, data: D): FirImport {
        return transformElement(import, data)
    }

    final override fun visitImport(import: FirImport, data: D): FirImport {
        return transformImport(import, data)
    }

    open fun transformResolvedImport(resolvedImport: FirResolvedImport, data: D): FirImport {
        return transformElement(resolvedImport, data)
    }

    final override fun visitResolvedImport(resolvedImport: FirResolvedImport, data: D): FirImport {
        return transformResolvedImport(resolvedImport, data)
    }

    open fun transformErrorImport(errorImport: FirErrorImport, data: D): FirImport {
        return transformElement(errorImport, data)
    }

    final override fun visitErrorImport(errorImport: FirErrorImport, data: D): FirImport {
        return transformErrorImport(errorImport, data)
    }

    open fun transformLoop(loop: FirLoop, data: D): FirStatement {
        return transformElement(loop, data)
    }

    final override fun visitLoop(loop: FirLoop, data: D): FirStatement {
        return transformLoop(loop, data)
    }

    open fun transformErrorLoop(errorLoop: FirErrorLoop, data: D): FirStatement {
        return transformElement(errorLoop, data)
    }

    final override fun visitErrorLoop(errorLoop: FirErrorLoop, data: D): FirStatement {
        return transformErrorLoop(errorLoop, data)
    }

    open fun transformDoWhileLoop(doWhileLoop: FirDoWhileLoop, data: D): FirStatement {
        return transformElement(doWhileLoop, data)
    }

    final override fun visitDoWhileLoop(doWhileLoop: FirDoWhileLoop, data: D): FirStatement {
        return transformDoWhileLoop(doWhileLoop, data)
    }

    open fun transformWhileLoop(whileLoop: FirWhileLoop, data: D): FirStatement {
        return transformElement(whileLoop, data)
    }

    final override fun visitWhileLoop(whileLoop: FirWhileLoop, data: D): FirStatement {
        return transformWhileLoop(whileLoop, data)
    }

    open fun transformBlock(block: FirBlock, data: D): FirStatement {
        return transformElement(block, data)
    }

    final override fun visitBlock(block: FirBlock, data: D): FirStatement {
        return transformBlock(block, data)
    }

    open fun transformLazyBlock(lazyBlock: FirLazyBlock, data: D): FirStatement {
        return transformElement(lazyBlock, data)
    }

    final override fun visitLazyBlock(lazyBlock: FirLazyBlock, data: D): FirStatement {
        return transformLazyBlock(lazyBlock, data)
    }

    open fun transformBinaryLogicExpression(binaryLogicExpression: FirBinaryLogicExpression, data: D): FirStatement {
        return transformElement(binaryLogicExpression, data)
    }

    final override fun visitBinaryLogicExpression(binaryLogicExpression: FirBinaryLogicExpression, data: D): FirStatement {
        return transformBinaryLogicExpression(binaryLogicExpression, data)
    }

    open fun <E : FirTargetElement> transformJump(jump: FirJump<E>, data: D): FirStatement {
        return transformElement(jump, data)
    }

    final override fun <E : FirTargetElement> visitJump(jump: FirJump<E>, data: D): FirStatement {
        return transformJump(jump, data)
    }

    open fun transformLoopJump(loopJump: FirLoopJump, data: D): FirStatement {
        return transformElement(loopJump, data)
    }

    final override fun visitLoopJump(loopJump: FirLoopJump, data: D): FirStatement {
        return transformLoopJump(loopJump, data)
    }

    open fun transformBreakExpression(breakExpression: FirBreakExpression, data: D): FirStatement {
        return transformElement(breakExpression, data)
    }

    final override fun visitBreakExpression(breakExpression: FirBreakExpression, data: D): FirStatement {
        return transformBreakExpression(breakExpression, data)
    }

    open fun transformContinueExpression(continueExpression: FirContinueExpression, data: D): FirStatement {
        return transformElement(continueExpression, data)
    }

    final override fun visitContinueExpression(continueExpression: FirContinueExpression, data: D): FirStatement {
        return transformContinueExpression(continueExpression, data)
    }

    open fun transformCatch(catch: FirCatch, data: D): FirCatch {
        return transformElement(catch, data)
    }

    final override fun visitCatch(catch: FirCatch, data: D): FirCatch {
        return transformCatch(catch, data)
    }

    open fun transformTryExpression(tryExpression: FirTryExpression, data: D): FirStatement {
        return transformElement(tryExpression, data)
    }

    final override fun visitTryExpression(tryExpression: FirTryExpression, data: D): FirStatement {
        return transformTryExpression(tryExpression, data)
    }

    open fun <T> transformConstExpression(constExpression: FirConstExpression<T>, data: D): FirStatement {
        return transformElement(constExpression, data)
    }

    final override fun <T> visitConstExpression(constExpression: FirConstExpression<T>, data: D): FirStatement {
        return transformConstExpression(constExpression, data)
    }

    open fun transformTypeProjection(typeProjection: FirTypeProjection, data: D): FirTypeProjection {
        return transformElement(typeProjection, data)
    }

    final override fun visitTypeProjection(typeProjection: FirTypeProjection, data: D): FirTypeProjection {
        return transformTypeProjection(typeProjection, data)
    }

    open fun transformStarProjection(starProjection: FirStarProjection, data: D): FirTypeProjection {
        return transformElement(starProjection, data)
    }

    final override fun visitStarProjection(starProjection: FirStarProjection, data: D): FirTypeProjection {
        return transformStarProjection(starProjection, data)
    }

    open fun transformPlaceholderProjection(placeholderProjection: FirPlaceholderProjection, data: D): FirTypeProjection {
        return transformElement(placeholderProjection, data)
    }

    final override fun visitPlaceholderProjection(placeholderProjection: FirPlaceholderProjection, data: D): FirTypeProjection {
        return transformPlaceholderProjection(placeholderProjection, data)
    }

    open fun transformTypeProjectionWithVariance(typeProjectionWithVariance: FirTypeProjectionWithVariance, data: D): FirTypeProjection {
        return transformElement(typeProjectionWithVariance, data)
    }

    final override fun visitTypeProjectionWithVariance(typeProjectionWithVariance: FirTypeProjectionWithVariance, data: D): FirTypeProjection {
        return transformTypeProjectionWithVariance(typeProjectionWithVariance, data)
    }

    open fun transformArgumentList(argumentList: FirArgumentList, data: D): FirArgumentList {
        return transformElement(argumentList, data)
    }

    final override fun visitArgumentList(argumentList: FirArgumentList, data: D): FirArgumentList {
        return transformArgumentList(argumentList, data)
    }

    open fun transformCall(call: FirCall, data: D): FirStatement {
        return transformElement(call, data)
    }

    final override fun visitCall(call: FirCall, data: D): FirStatement {
        return transformCall(call, data)
    }

    open fun transformAnnotation(annotation: FirAnnotation, data: D): FirStatement {
        return transformElement(annotation, data)
    }

    final override fun visitAnnotation(annotation: FirAnnotation, data: D): FirStatement {
        return transformAnnotation(annotation, data)
    }

    open fun transformAnnotationCall(annotationCall: FirAnnotationCall, data: D): FirStatement {
        return transformElement(annotationCall, data)
    }

    final override fun visitAnnotationCall(annotationCall: FirAnnotationCall, data: D): FirStatement {
        return transformAnnotationCall(annotationCall, data)
    }

    open fun transformAnnotationArgumentMapping(annotationArgumentMapping: FirAnnotationArgumentMapping, data: D): FirAnnotationArgumentMapping {
        return transformElement(annotationArgumentMapping, data)
    }

    final override fun visitAnnotationArgumentMapping(annotationArgumentMapping: FirAnnotationArgumentMapping, data: D): FirAnnotationArgumentMapping {
        return transformAnnotationArgumentMapping(annotationArgumentMapping, data)
    }

    open fun transformErrorAnnotationCall(errorAnnotationCall: FirErrorAnnotationCall, data: D): FirStatement {
        return transformElement(errorAnnotationCall, data)
    }

    final override fun visitErrorAnnotationCall(errorAnnotationCall: FirErrorAnnotationCall, data: D): FirStatement {
        return transformErrorAnnotationCall(errorAnnotationCall, data)
    }

    open fun transformComparisonExpression(comparisonExpression: FirComparisonExpression, data: D): FirStatement {
        return transformElement(comparisonExpression, data)
    }

    final override fun visitComparisonExpression(comparisonExpression: FirComparisonExpression, data: D): FirStatement {
        return transformComparisonExpression(comparisonExpression, data)
    }

    open fun transformTypeOperatorCall(typeOperatorCall: FirTypeOperatorCall, data: D): FirStatement {
        return transformElement(typeOperatorCall, data)
    }

    final override fun visitTypeOperatorCall(typeOperatorCall: FirTypeOperatorCall, data: D): FirStatement {
        return transformTypeOperatorCall(typeOperatorCall, data)
    }

    open fun transformAssignmentOperatorStatement(assignmentOperatorStatement: FirAssignmentOperatorStatement, data: D): FirStatement {
        return transformElement(assignmentOperatorStatement, data)
    }

    final override fun visitAssignmentOperatorStatement(assignmentOperatorStatement: FirAssignmentOperatorStatement, data: D): FirStatement {
        return transformAssignmentOperatorStatement(assignmentOperatorStatement, data)
    }

    open fun transformIncrementDecrementExpression(incrementDecrementExpression: FirIncrementDecrementExpression, data: D): FirStatement {
        return transformElement(incrementDecrementExpression, data)
    }

    final override fun visitIncrementDecrementExpression(incrementDecrementExpression: FirIncrementDecrementExpression, data: D): FirStatement {
        return transformIncrementDecrementExpression(incrementDecrementExpression, data)
    }

    open fun transformEqualityOperatorCall(equalityOperatorCall: FirEqualityOperatorCall, data: D): FirStatement {
        return transformElement(equalityOperatorCall, data)
    }

    final override fun visitEqualityOperatorCall(equalityOperatorCall: FirEqualityOperatorCall, data: D): FirStatement {
        return transformEqualityOperatorCall(equalityOperatorCall, data)
    }

    open fun transformWhenExpression(whenExpression: FirWhenExpression, data: D): FirStatement {
        return transformElement(whenExpression, data)
    }

    final override fun visitWhenExpression(whenExpression: FirWhenExpression, data: D): FirStatement {
        return transformWhenExpression(whenExpression, data)
    }

    open fun transformWhenBranch(whenBranch: FirWhenBranch, data: D): FirWhenBranch {
        return transformElement(whenBranch, data)
    }

    final override fun visitWhenBranch(whenBranch: FirWhenBranch, data: D): FirWhenBranch {
        return transformWhenBranch(whenBranch, data)
    }

    open fun transformContextReceiverArgumentListOwner(contextReceiverArgumentListOwner: FirContextReceiverArgumentListOwner, data: D): FirContextReceiverArgumentListOwner {
        return transformElement(contextReceiverArgumentListOwner, data)
    }

    final override fun visitContextReceiverArgumentListOwner(contextReceiverArgumentListOwner: FirContextReceiverArgumentListOwner, data: D): FirContextReceiverArgumentListOwner {
        return transformContextReceiverArgumentListOwner(contextReceiverArgumentListOwner, data)
    }

    open fun transformCheckNotNullCall(checkNotNullCall: FirCheckNotNullCall, data: D): FirStatement {
        return transformElement(checkNotNullCall, data)
    }

    final override fun visitCheckNotNullCall(checkNotNullCall: FirCheckNotNullCall, data: D): FirStatement {
        return transformCheckNotNullCall(checkNotNullCall, data)
    }

    open fun transformElvisExpression(elvisExpression: FirElvisExpression, data: D): FirStatement {
        return transformElement(elvisExpression, data)
    }

    final override fun visitElvisExpression(elvisExpression: FirElvisExpression, data: D): FirStatement {
        return transformElvisExpression(elvisExpression, data)
    }

    open fun transformArrayLiteral(arrayLiteral: FirArrayLiteral, data: D): FirStatement {
        return transformElement(arrayLiteral, data)
    }

    final override fun visitArrayLiteral(arrayLiteral: FirArrayLiteral, data: D): FirStatement {
        return transformArrayLiteral(arrayLiteral, data)
    }

    open fun transformAugmentedArraySetCall(augmentedArraySetCall: FirAugmentedArraySetCall, data: D): FirStatement {
        return transformElement(augmentedArraySetCall, data)
    }

    final override fun visitAugmentedArraySetCall(augmentedArraySetCall: FirAugmentedArraySetCall, data: D): FirStatement {
        return transformAugmentedArraySetCall(augmentedArraySetCall, data)
    }

    open fun transformClassReferenceExpression(classReferenceExpression: FirClassReferenceExpression, data: D): FirStatement {
        return transformElement(classReferenceExpression, data)
    }

    final override fun visitClassReferenceExpression(classReferenceExpression: FirClassReferenceExpression, data: D): FirStatement {
        return transformClassReferenceExpression(classReferenceExpression, data)
    }

    open fun transformErrorExpression(errorExpression: FirErrorExpression, data: D): FirStatement {
        return transformElement(errorExpression, data)
    }

    final override fun visitErrorExpression(errorExpression: FirErrorExpression, data: D): FirStatement {
        return transformErrorExpression(errorExpression, data)
    }

    open fun transformErrorFunction(errorFunction: FirErrorFunction, data: D): FirStatement {
        return transformElement(errorFunction, data)
    }

    final override fun visitErrorFunction(errorFunction: FirErrorFunction, data: D): FirStatement {
        return transformErrorFunction(errorFunction, data)
    }

    open fun transformErrorProperty(errorProperty: FirErrorProperty, data: D): FirStatement {
        return transformElement(errorProperty, data)
    }

    final override fun visitErrorProperty(errorProperty: FirErrorProperty, data: D): FirStatement {
        return transformErrorProperty(errorProperty, data)
    }

    open fun transformErrorPrimaryConstructor(errorPrimaryConstructor: FirErrorPrimaryConstructor, data: D): FirStatement {
        return transformElement(errorPrimaryConstructor, data)
    }

    final override fun visitErrorPrimaryConstructor(errorPrimaryConstructor: FirErrorPrimaryConstructor, data: D): FirStatement {
        return transformErrorPrimaryConstructor(errorPrimaryConstructor, data)
    }

    open fun transformDanglingModifierList(danglingModifierList: FirDanglingModifierList, data: D): FirDanglingModifierList {
        return transformElement(danglingModifierList, data)
    }

    final override fun visitDanglingModifierList(danglingModifierList: FirDanglingModifierList, data: D): FirDanglingModifierList {
        return transformDanglingModifierList(danglingModifierList, data)
    }

    open fun transformQualifiedAccessExpression(qualifiedAccessExpression: FirQualifiedAccessExpression, data: D): FirStatement {
        return transformElement(qualifiedAccessExpression, data)
    }

    final override fun visitQualifiedAccessExpression(qualifiedAccessExpression: FirQualifiedAccessExpression, data: D): FirStatement {
        return transformQualifiedAccessExpression(qualifiedAccessExpression, data)
    }

    open fun transformQualifiedErrorAccessExpression(qualifiedErrorAccessExpression: FirQualifiedErrorAccessExpression, data: D): FirStatement {
        return transformElement(qualifiedErrorAccessExpression, data)
    }

    final override fun visitQualifiedErrorAccessExpression(qualifiedErrorAccessExpression: FirQualifiedErrorAccessExpression, data: D): FirStatement {
        return transformQualifiedErrorAccessExpression(qualifiedErrorAccessExpression, data)
    }

    open fun transformPropertyAccessExpression(propertyAccessExpression: FirPropertyAccessExpression, data: D): FirStatement {
        return transformElement(propertyAccessExpression, data)
    }

    final override fun visitPropertyAccessExpression(propertyAccessExpression: FirPropertyAccessExpression, data: D): FirStatement {
        return transformPropertyAccessExpression(propertyAccessExpression, data)
    }

    open fun transformFunctionCall(functionCall: FirFunctionCall, data: D): FirStatement {
        return transformElement(functionCall, data)
    }

    final override fun visitFunctionCall(functionCall: FirFunctionCall, data: D): FirStatement {
        return transformFunctionCall(functionCall, data)
    }

    open fun transformIntegerLiteralOperatorCall(integerLiteralOperatorCall: FirIntegerLiteralOperatorCall, data: D): FirStatement {
        return transformElement(integerLiteralOperatorCall, data)
    }

    final override fun visitIntegerLiteralOperatorCall(integerLiteralOperatorCall: FirIntegerLiteralOperatorCall, data: D): FirStatement {
        return transformIntegerLiteralOperatorCall(integerLiteralOperatorCall, data)
    }

    open fun transformImplicitInvokeCall(implicitInvokeCall: FirImplicitInvokeCall, data: D): FirStatement {
        return transformElement(implicitInvokeCall, data)
    }

    final override fun visitImplicitInvokeCall(implicitInvokeCall: FirImplicitInvokeCall, data: D): FirStatement {
        return transformImplicitInvokeCall(implicitInvokeCall, data)
    }

    open fun transformDelegatedConstructorCall(delegatedConstructorCall: FirDelegatedConstructorCall, data: D): FirStatement {
        return transformElement(delegatedConstructorCall, data)
    }

    final override fun visitDelegatedConstructorCall(delegatedConstructorCall: FirDelegatedConstructorCall, data: D): FirStatement {
        return transformDelegatedConstructorCall(delegatedConstructorCall, data)
    }

    open fun transformMultiDelegatedConstructorCall(multiDelegatedConstructorCall: FirMultiDelegatedConstructorCall, data: D): FirStatement {
        return transformElement(multiDelegatedConstructorCall, data)
    }

    final override fun visitMultiDelegatedConstructorCall(multiDelegatedConstructorCall: FirMultiDelegatedConstructorCall, data: D): FirStatement {
        return transformMultiDelegatedConstructorCall(multiDelegatedConstructorCall, data)
    }

    open fun transformComponentCall(componentCall: FirComponentCall, data: D): FirStatement {
        return transformElement(componentCall, data)
    }

    final override fun visitComponentCall(componentCall: FirComponentCall, data: D): FirStatement {
        return transformComponentCall(componentCall, data)
    }

    open fun transformCallableReferenceAccess(callableReferenceAccess: FirCallableReferenceAccess, data: D): FirStatement {
        return transformElement(callableReferenceAccess, data)
    }

    final override fun visitCallableReferenceAccess(callableReferenceAccess: FirCallableReferenceAccess, data: D): FirStatement {
        return transformCallableReferenceAccess(callableReferenceAccess, data)
    }

    open fun transformThisReceiverExpression(thisReceiverExpression: FirThisReceiverExpression, data: D): FirStatement {
        return transformElement(thisReceiverExpression, data)
    }

    final override fun visitThisReceiverExpression(thisReceiverExpression: FirThisReceiverExpression, data: D): FirStatement {
        return transformThisReceiverExpression(thisReceiverExpression, data)
    }

    open fun transformInaccessibleReceiverExpression(inaccessibleReceiverExpression: FirInaccessibleReceiverExpression, data: D): FirStatement {
        return transformElement(inaccessibleReceiverExpression, data)
    }

    final override fun visitInaccessibleReceiverExpression(inaccessibleReceiverExpression: FirInaccessibleReceiverExpression, data: D): FirStatement {
        return transformInaccessibleReceiverExpression(inaccessibleReceiverExpression, data)
    }

    open fun transformSmartCastExpression(smartCastExpression: FirSmartCastExpression, data: D): FirStatement {
        return transformElement(smartCastExpression, data)
    }

    final override fun visitSmartCastExpression(smartCastExpression: FirSmartCastExpression, data: D): FirStatement {
        return transformSmartCastExpression(smartCastExpression, data)
    }

    open fun transformSafeCallExpression(safeCallExpression: FirSafeCallExpression, data: D): FirStatement {
        return transformElement(safeCallExpression, data)
    }

    final override fun visitSafeCallExpression(safeCallExpression: FirSafeCallExpression, data: D): FirStatement {
        return transformSafeCallExpression(safeCallExpression, data)
    }

    open fun transformCheckedSafeCallSubject(checkedSafeCallSubject: FirCheckedSafeCallSubject, data: D): FirStatement {
        return transformElement(checkedSafeCallSubject, data)
    }

    final override fun visitCheckedSafeCallSubject(checkedSafeCallSubject: FirCheckedSafeCallSubject, data: D): FirStatement {
        return transformCheckedSafeCallSubject(checkedSafeCallSubject, data)
    }

    open fun transformGetClassCall(getClassCall: FirGetClassCall, data: D): FirStatement {
        return transformElement(getClassCall, data)
    }

    final override fun visitGetClassCall(getClassCall: FirGetClassCall, data: D): FirStatement {
        return transformGetClassCall(getClassCall, data)
    }

    open fun transformWrappedExpression(wrappedExpression: FirWrappedExpression, data: D): FirStatement {
        return transformElement(wrappedExpression, data)
    }

    final override fun visitWrappedExpression(wrappedExpression: FirWrappedExpression, data: D): FirStatement {
        return transformWrappedExpression(wrappedExpression, data)
    }

    open fun transformWrappedArgumentExpression(wrappedArgumentExpression: FirWrappedArgumentExpression, data: D): FirStatement {
        return transformElement(wrappedArgumentExpression, data)
    }

    final override fun visitWrappedArgumentExpression(wrappedArgumentExpression: FirWrappedArgumentExpression, data: D): FirStatement {
        return transformWrappedArgumentExpression(wrappedArgumentExpression, data)
    }

    open fun transformLambdaArgumentExpression(lambdaArgumentExpression: FirLambdaArgumentExpression, data: D): FirStatement {
        return transformElement(lambdaArgumentExpression, data)
    }

    final override fun visitLambdaArgumentExpression(lambdaArgumentExpression: FirLambdaArgumentExpression, data: D): FirStatement {
        return transformLambdaArgumentExpression(lambdaArgumentExpression, data)
    }

    open fun transformSpreadArgumentExpression(spreadArgumentExpression: FirSpreadArgumentExpression, data: D): FirStatement {
        return transformElement(spreadArgumentExpression, data)
    }

    final override fun visitSpreadArgumentExpression(spreadArgumentExpression: FirSpreadArgumentExpression, data: D): FirStatement {
        return transformSpreadArgumentExpression(spreadArgumentExpression, data)
    }

    open fun transformNamedArgumentExpression(namedArgumentExpression: FirNamedArgumentExpression, data: D): FirStatement {
        return transformElement(namedArgumentExpression, data)
    }

    final override fun visitNamedArgumentExpression(namedArgumentExpression: FirNamedArgumentExpression, data: D): FirStatement {
        return transformNamedArgumentExpression(namedArgumentExpression, data)
    }

    open fun transformVarargArgumentsExpression(varargArgumentsExpression: FirVarargArgumentsExpression, data: D): FirStatement {
        return transformElement(varargArgumentsExpression, data)
    }

    final override fun visitVarargArgumentsExpression(varargArgumentsExpression: FirVarargArgumentsExpression, data: D): FirStatement {
        return transformVarargArgumentsExpression(varargArgumentsExpression, data)
    }

    open fun transformResolvedQualifier(resolvedQualifier: FirResolvedQualifier, data: D): FirStatement {
        return transformElement(resolvedQualifier, data)
    }

    final override fun visitResolvedQualifier(resolvedQualifier: FirResolvedQualifier, data: D): FirStatement {
        return transformResolvedQualifier(resolvedQualifier, data)
    }

    open fun transformErrorResolvedQualifier(errorResolvedQualifier: FirErrorResolvedQualifier, data: D): FirStatement {
        return transformElement(errorResolvedQualifier, data)
    }

    final override fun visitErrorResolvedQualifier(errorResolvedQualifier: FirErrorResolvedQualifier, data: D): FirStatement {
        return transformErrorResolvedQualifier(errorResolvedQualifier, data)
    }

    open fun transformResolvedReifiedParameterReference(resolvedReifiedParameterReference: FirResolvedReifiedParameterReference, data: D): FirStatement {
        return transformElement(resolvedReifiedParameterReference, data)
    }

    final override fun visitResolvedReifiedParameterReference(resolvedReifiedParameterReference: FirResolvedReifiedParameterReference, data: D): FirStatement {
        return transformResolvedReifiedParameterReference(resolvedReifiedParameterReference, data)
    }

    open fun transformReturnExpression(returnExpression: FirReturnExpression, data: D): FirStatement {
        return transformElement(returnExpression, data)
    }

    final override fun visitReturnExpression(returnExpression: FirReturnExpression, data: D): FirStatement {
        return transformReturnExpression(returnExpression, data)
    }

    open fun transformStringConcatenationCall(stringConcatenationCall: FirStringConcatenationCall, data: D): FirStatement {
        return transformElement(stringConcatenationCall, data)
    }

    final override fun visitStringConcatenationCall(stringConcatenationCall: FirStringConcatenationCall, data: D): FirStatement {
        return transformStringConcatenationCall(stringConcatenationCall, data)
    }

    open fun transformThrowExpression(throwExpression: FirThrowExpression, data: D): FirStatement {
        return transformElement(throwExpression, data)
    }

    final override fun visitThrowExpression(throwExpression: FirThrowExpression, data: D): FirStatement {
        return transformThrowExpression(throwExpression, data)
    }

    open fun transformVariableAssignment(variableAssignment: FirVariableAssignment, data: D): FirStatement {
        return transformElement(variableAssignment, data)
    }

    final override fun visitVariableAssignment(variableAssignment: FirVariableAssignment, data: D): FirStatement {
        return transformVariableAssignment(variableAssignment, data)
    }

    open fun transformWhenSubjectExpression(whenSubjectExpression: FirWhenSubjectExpression, data: D): FirStatement {
        return transformElement(whenSubjectExpression, data)
    }

    final override fun visitWhenSubjectExpression(whenSubjectExpression: FirWhenSubjectExpression, data: D): FirStatement {
        return transformWhenSubjectExpression(whenSubjectExpression, data)
    }

    open fun transformDesugaredAssignmentValueReferenceExpression(desugaredAssignmentValueReferenceExpression: FirDesugaredAssignmentValueReferenceExpression, data: D): FirStatement {
        return transformElement(desugaredAssignmentValueReferenceExpression, data)
    }

    final override fun visitDesugaredAssignmentValueReferenceExpression(desugaredAssignmentValueReferenceExpression: FirDesugaredAssignmentValueReferenceExpression, data: D): FirStatement {
        return transformDesugaredAssignmentValueReferenceExpression(desugaredAssignmentValueReferenceExpression, data)
    }

    open fun transformWrappedDelegateExpression(wrappedDelegateExpression: FirWrappedDelegateExpression, data: D): FirStatement {
        return transformElement(wrappedDelegateExpression, data)
    }

    final override fun visitWrappedDelegateExpression(wrappedDelegateExpression: FirWrappedDelegateExpression, data: D): FirStatement {
        return transformWrappedDelegateExpression(wrappedDelegateExpression, data)
    }

    open fun transformEnumEntryDeserializedAccessExpression(enumEntryDeserializedAccessExpression: FirEnumEntryDeserializedAccessExpression, data: D): FirStatement {
        return transformElement(enumEntryDeserializedAccessExpression, data)
    }

    final override fun visitEnumEntryDeserializedAccessExpression(enumEntryDeserializedAccessExpression: FirEnumEntryDeserializedAccessExpression, data: D): FirStatement {
        return transformEnumEntryDeserializedAccessExpression(enumEntryDeserializedAccessExpression, data)
    }

    open fun transformNamedReference(namedReference: FirNamedReference, data: D): FirReference {
        return transformElement(namedReference, data)
    }

    final override fun visitNamedReference(namedReference: FirNamedReference, data: D): FirReference {
        return transformNamedReference(namedReference, data)
    }

    open fun transformNamedReferenceWithCandidateBase(namedReferenceWithCandidateBase: FirNamedReferenceWithCandidateBase, data: D): FirReference {
        return transformElement(namedReferenceWithCandidateBase, data)
    }

    final override fun visitNamedReferenceWithCandidateBase(namedReferenceWithCandidateBase: FirNamedReferenceWithCandidateBase, data: D): FirReference {
        return transformNamedReferenceWithCandidateBase(namedReferenceWithCandidateBase, data)
    }

    open fun transformErrorNamedReference(errorNamedReference: FirErrorNamedReference, data: D): FirReference {
        return transformElement(errorNamedReference, data)
    }

    final override fun visitErrorNamedReference(errorNamedReference: FirErrorNamedReference, data: D): FirReference {
        return transformErrorNamedReference(errorNamedReference, data)
    }

    open fun transformFromMissingDependenciesNamedReference(fromMissingDependenciesNamedReference: FirFromMissingDependenciesNamedReference, data: D): FirReference {
        return transformElement(fromMissingDependenciesNamedReference, data)
    }

    final override fun visitFromMissingDependenciesNamedReference(fromMissingDependenciesNamedReference: FirFromMissingDependenciesNamedReference, data: D): FirReference {
        return transformFromMissingDependenciesNamedReference(fromMissingDependenciesNamedReference, data)
    }

    open fun transformSuperReference(superReference: FirSuperReference, data: D): FirReference {
        return transformElement(superReference, data)
    }

    final override fun visitSuperReference(superReference: FirSuperReference, data: D): FirReference {
        return transformSuperReference(superReference, data)
    }

    open fun transformThisReference(thisReference: FirThisReference, data: D): FirReference {
        return transformElement(thisReference, data)
    }

    final override fun visitThisReference(thisReference: FirThisReference, data: D): FirReference {
        return transformThisReference(thisReference, data)
    }

    open fun transformControlFlowGraphReference(controlFlowGraphReference: FirControlFlowGraphReference, data: D): FirReference {
        return transformElement(controlFlowGraphReference, data)
    }

    final override fun visitControlFlowGraphReference(controlFlowGraphReference: FirControlFlowGraphReference, data: D): FirReference {
        return transformControlFlowGraphReference(controlFlowGraphReference, data)
    }

    open fun transformResolvedNamedReference(resolvedNamedReference: FirResolvedNamedReference, data: D): FirReference {
        return transformElement(resolvedNamedReference, data)
    }

    final override fun visitResolvedNamedReference(resolvedNamedReference: FirResolvedNamedReference, data: D): FirReference {
        return transformResolvedNamedReference(resolvedNamedReference, data)
    }

    open fun transformResolvedErrorReference(resolvedErrorReference: FirResolvedErrorReference, data: D): FirReference {
        return transformElement(resolvedErrorReference, data)
    }

    final override fun visitResolvedErrorReference(resolvedErrorReference: FirResolvedErrorReference, data: D): FirReference {
        return transformResolvedErrorReference(resolvedErrorReference, data)
    }

    open fun transformDelegateFieldReference(delegateFieldReference: FirDelegateFieldReference, data: D): FirReference {
        return transformElement(delegateFieldReference, data)
    }

    final override fun visitDelegateFieldReference(delegateFieldReference: FirDelegateFieldReference, data: D): FirReference {
        return transformDelegateFieldReference(delegateFieldReference, data)
    }

    open fun transformBackingFieldReference(backingFieldReference: FirBackingFieldReference, data: D): FirReference {
        return transformElement(backingFieldReference, data)
    }

    final override fun visitBackingFieldReference(backingFieldReference: FirBackingFieldReference, data: D): FirReference {
        return transformBackingFieldReference(backingFieldReference, data)
    }

    open fun transformResolvedCallableReference(resolvedCallableReference: FirResolvedCallableReference, data: D): FirReference {
        return transformElement(resolvedCallableReference, data)
    }

    final override fun visitResolvedCallableReference(resolvedCallableReference: FirResolvedCallableReference, data: D): FirReference {
        return transformResolvedCallableReference(resolvedCallableReference, data)
    }

    open fun transformResolvedTypeRef(resolvedTypeRef: FirResolvedTypeRef, data: D): FirTypeRef {
        return transformElement(resolvedTypeRef, data)
    }

    final override fun visitResolvedTypeRef(resolvedTypeRef: FirResolvedTypeRef, data: D): FirTypeRef {
        return transformResolvedTypeRef(resolvedTypeRef, data)
    }

    open fun transformErrorTypeRef(errorTypeRef: FirErrorTypeRef, data: D): FirTypeRef {
        return transformElement(errorTypeRef, data)
    }

    final override fun visitErrorTypeRef(errorTypeRef: FirErrorTypeRef, data: D): FirTypeRef {
        return transformErrorTypeRef(errorTypeRef, data)
    }

    open fun transformTypeRefWithNullability(typeRefWithNullability: FirTypeRefWithNullability, data: D): FirTypeRef {
        return transformElement(typeRefWithNullability, data)
    }

    final override fun visitTypeRefWithNullability(typeRefWithNullability: FirTypeRefWithNullability, data: D): FirTypeRef {
        return transformTypeRefWithNullability(typeRefWithNullability, data)
    }

    open fun transformUserTypeRef(userTypeRef: FirUserTypeRef, data: D): FirTypeRef {
        return transformElement(userTypeRef, data)
    }

    final override fun visitUserTypeRef(userTypeRef: FirUserTypeRef, data: D): FirTypeRef {
        return transformUserTypeRef(userTypeRef, data)
    }

    open fun transformDynamicTypeRef(dynamicTypeRef: FirDynamicTypeRef, data: D): FirTypeRef {
        return transformElement(dynamicTypeRef, data)
    }

    final override fun visitDynamicTypeRef(dynamicTypeRef: FirDynamicTypeRef, data: D): FirTypeRef {
        return transformDynamicTypeRef(dynamicTypeRef, data)
    }

    open fun transformFunctionTypeRef(functionTypeRef: FirFunctionTypeRef, data: D): FirTypeRef {
        return transformElement(functionTypeRef, data)
    }

    final override fun visitFunctionTypeRef(functionTypeRef: FirFunctionTypeRef, data: D): FirTypeRef {
        return transformFunctionTypeRef(functionTypeRef, data)
    }

    open fun transformIntersectionTypeRef(intersectionTypeRef: FirIntersectionTypeRef, data: D): FirTypeRef {
        return transformElement(intersectionTypeRef, data)
    }

    final override fun visitIntersectionTypeRef(intersectionTypeRef: FirIntersectionTypeRef, data: D): FirTypeRef {
        return transformIntersectionTypeRef(intersectionTypeRef, data)
    }

    open fun transformImplicitTypeRef(implicitTypeRef: FirImplicitTypeRef, data: D): FirTypeRef {
        return transformElement(implicitTypeRef, data)
    }

    final override fun visitImplicitTypeRef(implicitTypeRef: FirImplicitTypeRef, data: D): FirTypeRef {
        return transformImplicitTypeRef(implicitTypeRef, data)
    }

    open fun transformContractElementDeclaration(contractElementDeclaration: FirContractElementDeclaration, data: D): FirContractElementDeclaration {
        return transformElement(contractElementDeclaration, data)
    }

    final override fun visitContractElementDeclaration(contractElementDeclaration: FirContractElementDeclaration, data: D): FirContractElementDeclaration {
        return transformContractElementDeclaration(contractElementDeclaration, data)
    }

    open fun transformEffectDeclaration(effectDeclaration: FirEffectDeclaration, data: D): FirContractElementDeclaration {
        return transformElement(effectDeclaration, data)
    }

    final override fun visitEffectDeclaration(effectDeclaration: FirEffectDeclaration, data: D): FirContractElementDeclaration {
        return transformEffectDeclaration(effectDeclaration, data)
    }

    open fun transformContractDescription(contractDescription: FirContractDescription, data: D): FirContractDescription {
        return transformElement(contractDescription, data)
    }

    final override fun visitContractDescription(contractDescription: FirContractDescription, data: D): FirContractDescription {
        return transformContractDescription(contractDescription, data)
    }

    open fun transformLegacyRawContractDescription(legacyRawContractDescription: FirLegacyRawContractDescription, data: D): FirContractDescription {
        return transformElement(legacyRawContractDescription, data)
    }

    final override fun visitLegacyRawContractDescription(legacyRawContractDescription: FirLegacyRawContractDescription, data: D): FirContractDescription {
        return transformLegacyRawContractDescription(legacyRawContractDescription, data)
    }

    open fun transformRawContractDescription(rawContractDescription: FirRawContractDescription, data: D): FirContractDescription {
        return transformElement(rawContractDescription, data)
    }

    final override fun visitRawContractDescription(rawContractDescription: FirRawContractDescription, data: D): FirContractDescription {
        return transformRawContractDescription(rawContractDescription, data)
    }

    open fun transformResolvedContractDescription(resolvedContractDescription: FirResolvedContractDescription, data: D): FirContractDescription {
        return transformElement(resolvedContractDescription, data)
    }

    final override fun visitResolvedContractDescription(resolvedContractDescription: FirResolvedContractDescription, data: D): FirContractDescription {
        return transformResolvedContractDescription(resolvedContractDescription, data)
    }
}
