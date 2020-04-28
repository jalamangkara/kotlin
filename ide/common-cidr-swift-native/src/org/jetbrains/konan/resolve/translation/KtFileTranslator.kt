package org.jetbrains.konan.resolve.translation

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.containers.MostlySingularMultiMap
import com.jetbrains.cidr.lang.CLanguageKind
import com.jetbrains.cidr.lang.OCLanguageKind
import com.jetbrains.cidr.lang.preprocessor.OCInclusionContext
import com.jetbrains.cidr.lang.symbols.OCSymbol
import com.jetbrains.swift.languageKind.SwiftLanguageKind
import org.jetbrains.konan.resolve.symbols.KtSymbol
import org.jetbrains.kotlin.analyzer.ModuleInfo
import org.jetbrains.kotlin.backend.konan.objcexport.*
import org.jetbrains.kotlin.idea.caches.resolve.getResolutionFacade
import org.jetbrains.kotlin.idea.resolve.frontendService
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.deprecation.DeprecationResolver
import org.jetbrains.kotlin.resolve.lazy.ResolveSession
import org.jetbrains.kotlin.util.getValueOrNull

abstract class KtFileTranslator<T : KtSymbol, M : OCSymbol> {
    fun translate(file: KtFile, frameworkName: String, destination: MutableList<in T>) {
        translate(file, frameworkName, destination, ObjCExportLazy::translate)
    }

    fun translateBase(file: KtFile, frameworkName: String, destination: MutableList<in T>) {
        translate(file, frameworkName, destination) { lazy, _ -> lazy.generateBase() }
    }

    fun translateMembers(containingStub: ObjCClass<*>, project: Project, containingClass: T): MostlySingularMultiMap<String, M>? {
        val map = lazy(LazyThreadSafetyMode.NONE) { MostlySingularMultiMap<String, M>() }
        for (member in containingStub.members) {
            translateMember(member, project, containingClass.containingFile, containingClass) {
                map.value.add(it.name, it)
            }
        }
        return map.getValueOrNull()
    }

    protected abstract fun translate(
        stubTrace: StubTrace, stubs: Collection<ObjCTopLevel<*>>, file: VirtualFile, destination: MutableList<in T>
    )

    protected abstract fun translateMember(stub: Stub<*>, project: Project, file: VirtualFile, containingClass: T, processor: (M) -> Unit)

    private class ObjCExportConfiguration(override val frameworkName: String) : ObjCExportLazy.Configuration {
        override fun getCompilerModuleName(moduleInfo: ModuleInfo): String =
            TODO() // no implementation in `KonanCompilerFrontendServices.kt` either

        override fun isIncluded(moduleInfo: ModuleInfo): Boolean =
            true // always return true in `KonanCompilerFrontendServices.kt` as well

        override val objcGenerics: Boolean get() = false
    }

    private inline fun translate(
        file: KtFile, frameworkName: String, destination: MutableList<in T>, provideStubs: (ObjCExportLazy, KtFile) -> List<ObjCTopLevel<*>>
    ) {
        val resolutionFacade = file.getResolutionFacade()
        val moduleDescriptor = resolutionFacade.moduleDescriptor
        val resolveSession = resolutionFacade.frontendService<ResolveSession>()
        val deprecationResolver = resolutionFacade.frontendService<DeprecationResolver>()

        val lazy = createObjCExportLazy(
            ObjCExportConfiguration(frameworkName),
            ObjCExportWarningCollector.SILENT,
            resolveSession,
            resolveSession.typeResolver,
            resolveSession.descriptorResolver,
            resolveSession.fileScopeProvider,
            moduleDescriptor.builtIns,
            deprecationResolver
        )

        translate(StubTrace(file.virtualFile, resolutionFacade, moduleDescriptor), provideStubs(lazy, file), file.virtualFile, destination)
    }

    companion object {
        @JvmField
        val PRELOADED_LANGUAGE_KINDS: Collection<OCLanguageKind> = listOf(CLanguageKind.OBJ_C, SwiftLanguageKind)

        @JvmStatic
        val OCInclusionContext.isKtTranslationSupported: Boolean
            get() = languageKind.let { it == SwiftLanguageKind || it is CLanguageKind }

        @JvmStatic
        val OCInclusionContext.ktTranslator: KtFileTranslator<*, *>
            get() = when (languageKind) {
                SwiftLanguageKind -> KtSwiftSymbolTranslator
                is CLanguageKind -> KtOCSymbolTranslator
                else -> throw UnsupportedOperationException("Unsupported language kind $languageKind")
            }
    }
}