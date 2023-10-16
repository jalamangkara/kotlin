/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.calls.mpp

import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.mpp.*
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.resolve.multiplatform.ExpectActualMatchingCompatibility
import org.jetbrains.kotlin.types.model.KotlinTypeMarker
import org.jetbrains.kotlin.types.model.TypeSubstitutorMarker
import org.jetbrains.kotlin.utils.SmartList
import org.jetbrains.kotlin.utils.addToStdlib.enumMapOf
import org.jetbrains.kotlin.utils.addToStdlib.enumSetOf
import org.jetbrains.kotlin.utils.keysToMap
import org.jetbrains.kotlin.utils.zipIfSizesAreEqual
import java.util.*

object AbstractExpectActualMatcher {
    //fun <T : DeclarationSymbolMarker> getClassifiersCompatibility(
    //    expectClassSymbol: RegularClassSymbolMarker,
    //    actualClassLikeSymbol: ClassLikeSymbolMarker,
    //    checkClassScopesCompatibility: Boolean,
    //    context: ExpectActualMatchingContext<T>,
    //): ExpectActualCompatibility<T> {
    //    val result = with(context) {
    //        getClassifiersCompatibility(expectClassSymbol, actualClassLikeSymbol, parentSubstitutor = null, checkClassScopesCompatibility)
    //    }
    //    @Suppress("UNCHECKED_CAST")
    //    return result as ExpectActualCompatibility<T>
    //}

    fun <T : DeclarationSymbolMarker> getCallablesMatchingCompatibility(
        expectDeclaration: CallableSymbolMarker,
        actualDeclaration: CallableSymbolMarker,
        expectContainingClass: RegularClassSymbolMarker?,
        actualContainingClass: RegularClassSymbolMarker?,
        context: ExpectActualMatchingContext<T>,
    ): ExpectActualMatchingCompatibility<T> = with (context) {
        val expectTypeParameters = expectContainingClass?.typeParameters.orEmpty()
        val actualTypeParameters = actualContainingClass?.typeParameters.orEmpty()
        val parentSubstitutor = (expectTypeParameters zipIfSizesAreEqual actualTypeParameters)
            ?.let { createExpectActualTypeParameterSubstitutor(it, parentSubstitutor = null) }
        val result = getCallablesCompatibility(
            expectDeclaration,
            actualDeclaration,
            parentSubstitutor,
            expectContainingClass,
            actualContainingClass
        )
        @Suppress("UNCHECKED_CAST")
        result as ExpectActualMatchingCompatibility<T>
    }

    fun <T : DeclarationSymbolMarker> matchSingleExpectTopLevelDeclarationAgainstPotentialActuals(
        expectDeclaration: DeclarationSymbolMarker,
        actualDeclarations: List<DeclarationSymbolMarker>,
        context: ExpectActualMatchingContext<T>,
        checkClassScopesCompatibility: Boolean,
    ) {
        with(context) {
            matchSingleExpectAgainstPotentialActuals(
                expectDeclaration,
                actualDeclarations,
                substitutor = null,
                expectClassSymbol = null,
                actualClassSymbol = null,
                mismatchedMembers = null,
            )
        }
    }

    //context(ExpectActualMatchingContext<*>)
    //@Suppress("warnings")
    //private fun getClassifiersCompatibility(
    //    expectClassSymbol: RegularClassSymbolMarker,
    //    actualClassLikeSymbol: ClassLikeSymbolMarker,
    //    parentSubstitutor: TypeSubstitutorMarker?,
    //    checkClassScopes: Boolean,
    //): ExpectActualCompatibility<*> = getClassifiersIncompatibility(expectClassSymbol, actualClassLikeSymbol, parentSubstitutor, checkClassScopes)
    //    ?: ExpectActualCompatibility.Compatible

    //context(ExpectActualMatchingContext<*>)
    //@Suppress("warnings")
    //private fun getClassifiersIncompatibility(
    //    expectClassSymbol: RegularClassSymbolMarker,
    //    actualClassLikeSymbol: ClassLikeSymbolMarker,
    //    parentSubstitutor: TypeSubstitutorMarker?,
    //    checkClassScopesCompatibility: Boolean,
    //): ExpectActualCompatibility.Incompatible.ExpectActualCheckingIncompatible<*>? {
    //    // Can't check FQ names here because nested expected class may be implemented via actual typealias's expansion with the other FQ name
    //    require(expectClassSymbol.name == actualClassLikeSymbol.name) {
    //        "This function should be invoked only for declarations with the same name: $expectClassSymbol, $actualClassLikeSymbol"
    //    }
    //
    //    val actualClass = when (actualClassLikeSymbol) {
    //        is RegularClassSymbolMarker -> actualClassLikeSymbol
    //        is TypeAliasSymbolMarker -> actualClassLikeSymbol.expandToRegularClass()
    //            ?: return null // do not report extra error on erroneous typealias
    //        else -> error("Incorrect actual classifier for $expectClassSymbol: $actualClassLikeSymbol")
    //    }
    //
    //    if (!areCompatibleClassKinds(expectClassSymbol, actualClass)) return Incompatible.ClassKind
    //
    //    if (!equalBy(expectClassSymbol, actualClass) { listOf(it.isCompanion, it.isInner, it.isInline || it.isValue) }) {
    //        return Incompatible.ClassModifiers
    //    }
    //
    //    if (expectClassSymbol.isFun && !actualClass.isFun && actualClass.isNotSamInterface()) {
    //        return Incompatible.FunInterfaceModifier
    //    }
    //
    //    val expectTypeParameterSymbols = expectClassSymbol.typeParameters
    //    val actualTypeParameterSymbols = actualClass.typeParameters
    //    if (expectTypeParameterSymbols.size != actualTypeParameterSymbols.size) {
    //        return Incompatible.ClassTypeParameterCount
    //    }
    //
    //    if (!areCompatibleModalities(expectClassSymbol.modality, actualClass.modality)) {
    //        return Incompatible.Modality
    //    }
    //
    //    if (!areCompatibleClassVisibilities(expectClassSymbol, actualClass)) {
    //        return Incompatible.Visibility
    //    }
    //
    //    val substitutor = createExpectActualTypeParameterSubstitutor(
    //        (expectTypeParameterSymbols zipIfSizesAreEqual actualTypeParameterSymbols)
    //            ?: error("expect/actual type parameters sizes are checked earlier"),
    //        parentSubstitutor
    //    )
    //
    //    if (!areCompatibleTypeParameterUpperBounds(expectTypeParameterSymbols, actualTypeParameterSymbols, substitutor)) {
    //        return Incompatible.ClassTypeParameterUpperBounds
    //    }
    //
    //    getTypeParametersVarianceOrReifiedIncompatibility(expectTypeParameterSymbols, actualTypeParameterSymbols)
    //        ?.let { return it }
    //
    //    if (!areCompatibleSupertypes(expectClassSymbol, actualClass, substitutor)) {
    //        return Incompatible.Supertypes
    //    }
    //
    //    if (checkClassScopesCompatibility) {
    //        getClassScopesIncompatibility(expectClassSymbol, actualClass, substitutor)?.let { return it }
    //    }
    //
    //    return null
    //}

    //context(ExpectActualMatchingContext<*>)
    //private fun areCompatibleSupertypes(
    //    expectClassSymbol: RegularClassSymbolMarker,
    //    actualClassSymbol: RegularClassSymbolMarker,
    //    substitutor: TypeSubstitutorMarker,
    //): Boolean {
    //    return when (allowTransitiveSupertypesActualization) {
    //        false -> areCompatibleSupertypesOneByOne(expectClassSymbol, actualClassSymbol, substitutor)
    //        true -> areCompatibleSupertypesTransitive(expectClassSymbol, actualClassSymbol, substitutor)
    //    }
    //}

    context(ExpectActualMatchingContext<*>)
    private fun areCompatibleSupertypesOneByOne(
        expectClassSymbol: RegularClassSymbolMarker,
        actualClassSymbol: RegularClassSymbolMarker,
        substitutor: TypeSubstitutorMarker,
    ): Boolean {
        // Subtract kotlin.Any from supertypes because it's implicitly added if no explicit supertype is specified,
        // and not added if an explicit supertype _is_ specified
        val expectSupertypes = expectClassSymbol.superTypes.filterNot { it.typeConstructor().isAnyConstructor() }
        val actualSupertypes = actualClassSymbol.superTypes.filterNot { it.typeConstructor().isAnyConstructor() }
        return expectSupertypes.all { expectSupertype ->
            val substitutedExpectType = substitutor.safeSubstitute(expectSupertype)
            actualSupertypes.any { actualSupertype ->
                areCompatibleExpectActualTypes(substitutedExpectType, actualSupertype)
            }
        }
    }

    context(ExpectActualMatchingContext<*>)
    private fun areCompatibleSupertypesTransitive(
        expectClassSymbol: RegularClassSymbolMarker,
        actualClassSymbol: RegularClassSymbolMarker,
        substitutor: TypeSubstitutorMarker,
    ): Boolean {
        val expectSupertypes = expectClassSymbol.superTypes.filterNot { it.typeConstructor().isAnyConstructor() }
        val actualType = actualClassSymbol.defaultType
        return expectSupertypes.all { expectSupertype ->
            actualTypeIsSubtypeOfExpectType(
                expectType = substitutor.safeSubstitute(expectSupertype),
                actualType = actualType
            )
        }
    }

    //context(ExpectActualMatchingContext<*>)
    //private fun getClassScopesIncompatibility(
    //    expectClassSymbol: RegularClassSymbolMarker,
    //    actualClassSymbol: RegularClassSymbolMarker,
    //    substitutor: TypeSubstitutorMarker,
    //): ExpectActualCheckingCompatibility.Incompatible<*>? {
    //    val unfulfilled = arrayListOf<Pair<DeclarationSymbolMarker, Map<ExpectActualMatchingCompatibility.Mismatch<*>, List<DeclarationSymbolMarker?>>>>()
    //
    //    val actualMembersByName = actualClassSymbol.collectAllMembers(isActualDeclaration = true).groupBy { it.name }
    //
    //    outer@ for (expectMember in expectClassSymbol.collectAllMembers(isActualDeclaration = false)) {
    //        if (expectMember is CallableSymbolMarker && expectMember.shouldSkipMatching(expectClassSymbol)) continue
    //
    //        val actualMembers = actualMembersByName[expectMember.name]?.filter { actualMember ->
    //            expectMember is CallableSymbolMarker && actualMember is CallableSymbolMarker ||
    //                    expectMember is RegularClassSymbolMarker && actualMember is RegularClassSymbolMarker
    //        }.orEmpty()
    //
    //        matchSingleExpectAgainstPotentialActuals(
    //            expectMember,
    //            actualMembers,
    //            substitutor,
    //            expectClassSymbol,
    //            actualClassSymbol,
    //            unfulfilled,
    //            checkClassScopesCompatibility = true,
    //        )
    //    }
    //
    //    if (expectClassSymbol.classKind == ClassKind.ENUM_CLASS) {
    //        val aEntries = expectClassSymbol.collectEnumEntryNames()
    //        val bEntries = actualClassSymbol.collectEnumEntryNames()
    //
    //        if (!bEntries.containsAll(aEntries)) return ExpectActualCheckingCompatibility.EnumEntries
    //    }
    //
    //    // TODO: check static scope?
    //
    //    if (unfulfilled.isEmpty()) return null
    //
    //    return ExpectActualCheckingCompatibility.ClassScopes(unfulfilled)
    //}

    context(ExpectActualMatchingContext<*>)
    internal fun matchSingleExpectAgainstPotentialActuals(
        expectMember: DeclarationSymbolMarker,
        actualMembers: List<DeclarationSymbolMarker>,
        substitutor: TypeSubstitutorMarker?,
        expectClassSymbol: RegularClassSymbolMarker?,
        actualClassSymbol: RegularClassSymbolMarker?,
        mismatchedMembers: MutableList<Pair<DeclarationSymbolMarker, Map<ExpectActualMatchingCompatibility.Mismatch<*>, List<DeclarationSymbolMarker?>>>>?,
    ): DeclarationSymbolMarker? {
        val mapping = actualMembers.keysToMap { actualMember ->
            when (expectMember) {
                is CallableSymbolMarker -> getCallablesCompatibility(
                    expectMember,
                    actualMember as CallableSymbolMarker,
                    substitutor,
                    expectClassSymbol,
                    actualClassSymbol
                )

                is RegularClassSymbolMarker -> {
                    val parentSubstitutor = substitutor?.takeIf { !innerClassesCapturesOuterTypeParameters }
                    //getClassifiersCompatibility(
                    //    expectMember,
                    //    actualMember as ClassLikeSymbolMarker,
                    //    parentSubstitutor,
                    //    checkClassScopesCompatibility,
                    //)
                    ExpectActualMatchingCompatibility.MatchedSuccessfully
                }
                else -> error("Unsupported declaration: $expectMember ($actualMembers)")
            }
        }

        val incompatibilityMap = mutableMapOf<ExpectActualMatchingCompatibility.Mismatch<*>, MutableList<DeclarationSymbolMarker>>()
        for ((actualMember, compatibility) in mapping) {
            when (compatibility) {
                ExpectActualMatchingCompatibility.MatchedSuccessfully -> {
                    onMatchedMembers(expectMember, actualMember, expectClassSymbol, actualClassSymbol)
                    return actualMember
                }

                is ExpectActualMatchingCompatibility.Mismatch -> incompatibilityMap.getOrPut(compatibility) { SmartList() }.add(actualMember)
            }
        }

        mismatchedMembers?.add(expectMember to incompatibilityMap)
        onMismatchedMembersFromClassScope(expectMember, incompatibilityMap, expectClassSymbol, actualClassSymbol)
        return null
    }

    context(ExpectActualMatchingContext<*>)
    private fun getCallablesCompatibility(
        expectDeclaration: CallableSymbolMarker,
        actualDeclaration: CallableSymbolMarker,
        parentSubstitutor: TypeSubstitutorMarker?,
        expectContainingClass: RegularClassSymbolMarker?,
        actualContainingClass: RegularClassSymbolMarker?,
    ): ExpectActualMatchingCompatibility<*> {
        require(
            (expectDeclaration is ConstructorSymbolMarker && actualDeclaration is ConstructorSymbolMarker) ||
                    expectDeclaration.callableId.callableName == actualDeclaration.callableId.callableName
        ) {
            "This function should be invoked only for declarations with the same name: $expectDeclaration, $actualDeclaration"
        }
        require((expectDeclaration.dispatchReceiverType == null) == (actualDeclaration.dispatchReceiverType == null)) {
            "This function should be invoked only for declarations in the same kind of container (both members or both top level): $expectDeclaration, $actualDeclaration"
        }

        if (
            enumConstructorsAreAlwaysCompatible &&
            expectContainingClass?.classKind == ClassKind.ENUM_CLASS &&
            actualContainingClass?.classKind == ClassKind.ENUM_CLASS &&
            expectDeclaration is ConstructorSymbolMarker &&
            actualDeclaration is ConstructorSymbolMarker
        ) {
            return ExpectActualMatchingCompatibility.MatchedSuccessfully
        }

        // We must prioritize to return STRONG incompatible over WEAK incompatible (because STRONG incompatibility allows to search for overloads)
        return getCallablesMatchingIncompatibility(expectDeclaration, actualDeclaration, parentSubstitutor)
            ?: ExpectActualMatchingCompatibility.MatchedSuccessfully
    }

    context(ExpectActualMatchingContext<*>)
    private fun getCallablesMatchingIncompatibility(
        expectDeclaration: CallableSymbolMarker,
        actualDeclaration: CallableSymbolMarker,
        parentSubstitutor: TypeSubstitutorMarker?,
    ): ExpectActualMatchingCompatibility.Mismatch<*>? {
        if (expectDeclaration is FunctionSymbolMarker != actualDeclaration is FunctionSymbolMarker) {
            return ExpectActualMatchingCompatibility.CallableKind
        }

        val expectedReceiverType = expectDeclaration.extensionReceiverType
        val actualReceiverType = actualDeclaration.extensionReceiverType
        if ((expectedReceiverType != null) != (actualReceiverType != null)) {
            return ExpectActualMatchingCompatibility.ParameterShape
        }

        val expectedValueParameters = expectDeclaration.valueParameters
        val actualValueParameters = actualDeclaration.valueParameters
        if (!valueParametersCountCompatible(expectDeclaration, actualDeclaration, expectedValueParameters, actualValueParameters)) {
            return ExpectActualMatchingCompatibility.ParameterCount
        }

        val expectedTypeParameters = expectDeclaration.typeParameters
        val actualTypeParameters = actualDeclaration.typeParameters
        if (expectedTypeParameters.size != actualTypeParameters.size) {
            return ExpectActualMatchingCompatibility.FunctionTypeParameterCount
        }

        val substitutor = createExpectActualTypeParameterSubstitutor(
            (expectedTypeParameters zipIfSizesAreEqual actualTypeParameters)
                ?: error("expect/actual type parameters sizes are checked earlier"),
            parentSubstitutor
        )

        if (
            !areCompatibleTypeLists(
                expectedValueParameters.toTypeList(substitutor),
                actualValueParameters.toTypeList(createEmptySubstitutor())
            ) ||
            !areCompatibleExpectActualTypes(
                expectedReceiverType?.let { substitutor.safeSubstitute(it) },
                actualReceiverType
            )
        ) {
            return ExpectActualMatchingCompatibility.ParameterTypes
        }

        if (!areCompatibleTypeParameterUpperBounds(expectedTypeParameters, actualTypeParameters, substitutor)) {
            return ExpectActualMatchingCompatibility.FunctionTypeParameterUpperBounds
        }

        return null
    }

    //context(ExpectActualMatchingContext<*>)
    //private fun getCallablesWeakIncompatibility(
    //    expectDeclaration: CallableSymbolMarker,
    //    actualDeclaration: CallableSymbolMarker,
    //    parentSubstitutor: TypeSubstitutorMarker?,
    //    expectContainingClass: RegularClassSymbolMarker?,
    //    actualContainingClass: RegularClassSymbolMarker?,
    //): Incompatible.ExpectActualCheckingIncompatible<*>? {
    //    val expectedTypeParameters = expectDeclaration.typeParameters
    //    val actualTypeParameters = actualDeclaration.typeParameters
    //    val expectedValueParameters = expectDeclaration.valueParameters
    //    val actualValueParameters = actualDeclaration.valueParameters
    //
    //    if (shouldCheckReturnTypesOfCallables) {
    //        val substitutor = createExpectActualTypeParameterSubstitutor(
    //            (expectedTypeParameters zipIfSizesAreEqual actualTypeParameters)
    //                ?: error("expect/actual type parameters sizes are checked earlier"),
    //            parentSubstitutor
    //        )
    //
    //        if (!areCompatibleExpectActualTypes(substitutor.safeSubstitute(expectDeclaration.returnType), actualDeclaration.returnType)) {
    //            return Incompatible.ReturnType
    //        }
    //    }
    //
    //    if (actualDeclaration.hasStableParameterNames && !equalsBy(expectedValueParameters, actualValueParameters) { it.name }) {
    //        return Incompatible.ParameterNames
    //    }
    //
    //    if (!equalsBy(expectedTypeParameters, actualTypeParameters) { it.name }) {
    //        return Incompatible.TypeParameterNames
    //    }
    //
    //    val expectModality = expectDeclaration.modality
    //    val actualModality = actualDeclaration.modality
    //    if (
    //        !areCompatibleModalities(
    //            expectModality,
    //            actualModality,
    //            expectContainingClass?.modality,
    //            actualContainingClass?.modality
    //        )
    //    ) {
    //        return Incompatible.Modality
    //    }
    //
    //    if (!areCompatibleCallableVisibilities(expectDeclaration.visibility, expectModality, actualDeclaration.visibility)) {
    //        return Incompatible.Visibility
    //    }
    //
    //    getTypeParametersVarianceOrReifiedIncompatibility(expectedTypeParameters, actualTypeParameters)?.let { return it }
    //
    //    if (shouldCheckAbsenceOfDefaultParamsInActual) {
    //        // "Default parameters in actual" check is required only for functions, because only functions can have parameters
    //        if (actualDeclaration is FunctionSymbolMarker && expectDeclaration is FunctionSymbolMarker) {
    //            // Actual annotation constructors can have default argument values; their consistency with arguments in the expected annotation
    //            // is checked in ExpectedActualDeclarationChecker.checkAnnotationConstructors
    //            if (!actualDeclaration.isAnnotationConstructor() &&
    //                // If default params came from common supertypes of actual class and expect class then it's a valid code.
    //                // Here we filter out such default params.
    //                (actualDeclaration.allOverriddenDeclarationsRecursive() - expectDeclaration.allOverriddenDeclarationsRecursive().toSet())
    //                    .flatMap { it.valueParameters }.any { it.hasDefaultValue }
    //            ) {
    //                return Incompatible.ActualFunctionWithDefaultParameters
    //            }
    //        }
    //    }
    //
    //    if (!equalsBy(expectedValueParameters, actualValueParameters) { it.isVararg }) {
    //        return Incompatible.ValueParameterVararg
    //    }
    //
    //    // Adding noinline/crossinline to parameters is disallowed, except if the expected declaration was not inline at all
    //    if (expectDeclaration is SimpleFunctionSymbolMarker && expectDeclaration.isInline) {
    //        if (expectedValueParameters.indices.any { i -> !expectedValueParameters[i].isNoinline && actualValueParameters[i].isNoinline }) {
    //            return Incompatible.ValueParameterNoinline
    //        }
    //        if (expectedValueParameters.indices.any { i -> !expectedValueParameters[i].isCrossinline && actualValueParameters[i].isCrossinline }) {
    //            return Incompatible.ValueParameterCrossinline
    //        }
    //    }
    //
    //    when {
    //        expectDeclaration is FunctionSymbolMarker && actualDeclaration is FunctionSymbolMarker ->
    //            getFunctionsIncompatibility(expectDeclaration, actualDeclaration)?.let { return it }
    //
    //        expectDeclaration is PropertySymbolMarker && actualDeclaration is PropertySymbolMarker ->
    //            getPropertiesIncompatibility(expectDeclaration, actualDeclaration)?.let { return it }
    //
    //        expectDeclaration is EnumEntrySymbolMarker && actualDeclaration is EnumEntrySymbolMarker -> {
    //            // do nothing, entries are matched only by name
    //        }
    //
    //        else -> error("Unsupported declarations: $expectDeclaration, $actualDeclaration")
    //    }
    //
    //    return null
    //}

    context(ExpectActualMatchingContext<*>)
    private fun valueParametersCountCompatible(
        expectDeclaration: CallableSymbolMarker,
        actualDeclaration: CallableSymbolMarker,
        expectValueParameters: List<ValueParameterSymbolMarker>,
        actualValueParameters: List<ValueParameterSymbolMarker>,
    ): Boolean {
        if (expectValueParameters.size == actualValueParameters.size) return true

        return if (expectDeclaration.isAnnotationConstructor() && actualDeclaration.isAnnotationConstructor()) {
            expectValueParameters.isEmpty() && actualValueParameters.all { it.hasDefaultValue }
        } else {
            false
        }
    }

    context(ExpectActualMatchingContext<*>)
    private fun areCompatibleTypeLists(
        expectedTypes: List<KotlinTypeMarker?>,
        actualTypes: List<KotlinTypeMarker?>,
    ): Boolean {
        for (i in expectedTypes.indices) {
            if (!areCompatibleExpectActualTypes(expectedTypes[i], actualTypes[i])) {
                return false
            }
        }
        return true
    }

    context(ExpectActualMatchingContext<*>)
    private fun areCompatibleClassKinds(
        expectClass: RegularClassSymbolMarker,
        actualClass: RegularClassSymbolMarker,
    ): Boolean {
        if (expectClass.classKind == actualClass.classKind) return true

        if (expectClass.classKind == ClassKind.CLASS && expectClass.isFinal && expectClass.isCtorless) {
            if (actualClass.classKind == ClassKind.OBJECT) return true
        }

        return false
    }

    private fun areCompatibleModalities(
        expectModality: Modality?,
        actualModality: Modality?,
        expectContainingClassModality: Modality? = null,
        actualContainingClassModality: Modality? = null,
    ): Boolean {
        val expectEffectiveModality = effectiveModality(expectModality, expectContainingClassModality)
        val actualEffectiveModality = effectiveModality(actualModality, actualContainingClassModality)

        return actualEffectiveModality in compatibleModalityMap.getValue(expectEffectiveModality)
    }

    /*
     * If containing class is final then all declarations in it effectively final
     */
    private fun effectiveModality(declarationModality: Modality?, containingClassModality: Modality?): Modality? {
        return when (containingClassModality) {
            Modality.FINAL -> Modality.FINAL
            else -> declarationModality
        }
    }

    /*
     * Key is expect modality, value is a set of compatible actual modalities
     */
    private val compatibleModalityMap: EnumMap<Modality, EnumSet<Modality>> = enumMapOf(
        Modality.ABSTRACT to enumSetOf(Modality.ABSTRACT),
        Modality.OPEN to enumSetOf(Modality.OPEN),
        Modality.FINAL to enumSetOf(Modality.OPEN, Modality.FINAL),
        Modality.SEALED to enumSetOf(Modality.SEALED),
    )

    //private fun areCompatibleCallableVisibilities(
    //    expectVisibility: Visibility,
    //    expectModality: Modality?,
    //    actualVisibility: Visibility,
    //): Boolean {
    //    val compare = Visibilities.compare(expectVisibility, actualVisibility)
    //    return if (expectModality != Modality.FINAL) {
    //        // For overridable declarations visibility should match precisely, see KT-19664
    //        compare == 0
    //    } else {
    //        // For non-overridable declarations actuals are allowed to have more permissive visibility
    //        compare != null && compare <= 0
    //    }
    //}

    //context(ExpectActualMatchingContext<*>)
    //private fun areCompatibleClassVisibilities(
    //    expectClassSymbol: RegularClassSymbolMarker,
    //    actualClassSymbol: RegularClassSymbolMarker,
    //): Boolean {
    //    val expectVisibility = expectClassSymbol.visibility
    //    val actualVisibility = actualClassSymbol.visibility
    //    if (expectVisibility == actualVisibility) return true
    //    if (!allowClassActualizationWithWiderVisibility) return false
    //    val result = Visibilities.compare(actualVisibility, expectVisibility)
    //    return result != null && result > 0
    //}

    context(ExpectActualMatchingContext<*>)
    private fun areCompatibleTypeParameterUpperBounds(
        expectTypeParameterSymbols: List<TypeParameterSymbolMarker>,
        actualTypeParameterSymbols: List<TypeParameterSymbolMarker>,
        substitutor: TypeSubstitutorMarker,
    ): Boolean {
        for (i in expectTypeParameterSymbols.indices) {
            val expectBounds = expectTypeParameterSymbols[i].bounds
            val actualBounds = actualTypeParameterSymbols[i].bounds
            if (
                expectBounds.size != actualBounds.size ||
                !areCompatibleTypeLists(expectBounds.map { substitutor.safeSubstitute(it) }, actualBounds)
            ) {
                return false
            }
        }

        return true
    }

    //context(ExpectActualMatchingContext<*>)
    //private fun getTypeParametersVarianceOrReifiedIncompatibility(
    //    expectTypeParameterSymbols: List<TypeParameterSymbolMarker>,
    //    actualTypeParameterSymbols: List<TypeParameterSymbolMarker>,
    //): ExpectActualCheckingCompatibility.Incompatible<*>? {
    //    if (!equalsBy(expectTypeParameterSymbols, actualTypeParameterSymbols) { it.variance }) {
    //        return ExpectActualCheckingCompatibility.TypeParameterVariance
    //    }
    //
    //    // Removing "reified" from an expected function's type parameter is fine
    //    if (
    //        expectTypeParameterSymbols.indices.any { i ->
    //            !expectTypeParameterSymbols[i].isReified && actualTypeParameterSymbols[i].isReified
    //        }
    //    ) {
    //        return ExpectActualCheckingCompatibility.TypeParameterReified
    //    }
    //
    //    return null
    //}

    //context(ExpectActualMatchingContext<*>)
    //private fun getFunctionsIncompatibility(
    //    expectFunction: CallableSymbolMarker,
    //    actualFunction: CallableSymbolMarker,
    //): Incompatible.ExpectActualCheckingIncompatible<*>? {
    //    if (!equalBy(expectFunction, actualFunction) { f -> f.isSuspend }) {
    //        return Incompatible.FunctionModifiersDifferent
    //    }
    //
    //    if (
    //        expectFunction.isInfix && !actualFunction.isInfix ||
    //        expectFunction.isInline && !actualFunction.isInline ||
    //        expectFunction.isOperator && !actualFunction.isOperator
    //    ) {
    //        return Incompatible.FunctionModifiersNotSubset
    //    }
    //
    //    return null
    //}

    //context(ExpectActualMatchingContext<*>)
    //private fun getPropertiesIncompatibility(
    //    expected: PropertySymbolMarker,
    //    actual: PropertySymbolMarker,
    //): Incompatible.ExpectActualCheckingIncompatible<*>? {
    //    return when {
    //        !equalBy(expected, actual) { p -> p.isVar } -> Incompatible.PropertyKind
    //        !equalBy(expected, actual) { p -> p.isLateinit } -> Incompatible.PropertyLateinitModifier
    //        expected.isConst && !actual.isConst -> Incompatible.PropertyConstModifier
    //        !arePropertySettersWithCompatibleVisibilities(expected, actual) -> Incompatible.PropertySetterVisibility
    //        else -> null
    //    }
    //}

    //context(ExpectActualMatchingContext<*>)
    //private fun arePropertySettersWithCompatibleVisibilities(
    //    expected: PropertySymbolMarker,
    //    actual: PropertySymbolMarker,
    //): Boolean {
    //    val expectedSetter = expected.setter ?: return true
    //    val actualSetter = actual.setter ?: return true
    //    return areCompatibleCallableVisibilities(expectedSetter.visibility, expectedSetter.modality, actualSetter.visibility)
    //}

    // ---------------------------------------- Utils ----------------------------------------

    context(ExpectActualMatchingContext<*>)
    private fun List<ValueParameterSymbolMarker>.toTypeList(substitutor: TypeSubstitutorMarker): List<KotlinTypeMarker> {
        return this.map { substitutor.safeSubstitute(it.returnType) }
    }

    private inline fun <T, K> equalsBy(first: List<T>, second: List<T>, selector: (T) -> K): Boolean {
        for (i in first.indices) {
            if (selector(first[i]) != selector(second[i])) return false
        }

        return true
    }

    private inline fun <T, K> equalBy(first: T, second: T, selector: (T) -> K): Boolean =
        selector(first) == selector(second)

    context(ExpectActualMatchingContext<*>)
    private val DeclarationSymbolMarker.name: Name
        get() = when (this) {
            is ConstructorSymbolMarker -> SpecialNames.INIT
            is ValueParameterSymbolMarker -> parameterName
            is CallableSymbolMarker -> callableId.callableName
            is RegularClassSymbolMarker -> classId.shortClassName
            is TypeAliasSymbolMarker -> classId.shortClassName
            is TypeParameterSymbolMarker -> parameterName
            else -> error("Unsupported declaration: $this")
        }

    context(ExpectActualMatchingContext<*>)
    private val RegularClassSymbolMarker.isCtorless: Boolean
        get() = getMembersForExpectClass(SpecialNames.INIT).isEmpty()

    context(ExpectActualMatchingContext<*>)
    private val RegularClassSymbolMarker.isFinal: Boolean
        get() = modality == Modality.FINAL
}
