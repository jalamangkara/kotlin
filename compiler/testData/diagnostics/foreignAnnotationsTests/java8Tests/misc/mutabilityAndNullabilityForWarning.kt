// NULLABILITY_ANNOTATIONS: @org.jetbrains.annotations:warn
// DIAGNOSTICS: -UNUSED_PARAMETER

// FILE: J.java
import java.util.List;

public class J {
    @org.jetbrains.annotations.ReadOnly
    @org.jetbrains.annotations.Nullable
    public static List<String> foo() {
        return null;
    }
}

// FILE: main.kt
fun main() {
    takeMutable(<!NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS!>J.foo()<!>)
}

fun takeMutable(l: MutableList<String>) {}