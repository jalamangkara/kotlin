// TARGET_BACKEND: JVM
// IGNORE_K1_K2_ABI_DIFFERENCE: KT-62558

// FILE: Definitions.kt
import interop.*

object Definitions {
    const val KT_CONSTANT = Interface.CONSTANT

    val ktValue = Interface.CONSTANT
}

fun box(): String =
    Definitions.ktValue

// FILE: interop/Interface.java
package interop;

public class Interface {
    public static final String CONSTANT = "OK";
}
