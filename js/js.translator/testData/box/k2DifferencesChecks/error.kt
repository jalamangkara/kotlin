// ORIGINAL: /compiler/testData/diagnostics/testsWithJsStdLib/jsCode/error.fir.kt
// WITH_STDLIB
val code = """
    var s = "hello"
    + );
"""

fun main(): Unit {
    js("var = 10;")

    js("""var = 10;""")

    js("""var
      = 777;
    """)

    js("""
    var = 777;
    """)

    js("var " + " = " + "10;")

    val n = 10
    js("var = $n;")

    js(code)
}


fun box() = "OK".also { main() }
