/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.renderer

import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol

open class FirSymbolRenderer {

    internal lateinit var components: FirRendererComponents
    protected val printer get() = components.printer

    open fun printReference(symbol: FirBasedSymbol<*>) {
        when (symbol) {
            is FirCallableSymbol<*> -> printer.print(symbol.callableId.toString())
            is FirClassLikeSymbol<*> -> printer.print(symbol.classId.toString())
            else -> printer.print("?")
        }
    }
}