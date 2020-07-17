import java.io.PrintWriter

fun generateCode(outputDir: String) {
    defineAst(outputDir, "Expr", listOf(
            "Binary | val left: Expr, val operator: Token, val right: Expr",
            "Grouping | val expression: Expr",
            "Literal | val value: Any?",
            "Unary | val operator: Token, val right: Expr"
    ))
}

fun defineAst(outputDir: String, baseName: String, types: List<String>) {
    val path = "$outputDir/$baseName.kt"
    val writer = PrintWriter(path, "UTF-8")

    writer.println("abstract class $baseName {")
    writer.println("    abstract fun <R> accept(visitor: Visitor<R>): R")

    defineVisitor(writer, baseName, types)

    for (type in types) {
        val parts = type.split("|")
        val className = parts[0].trim()
        val fields = parts[1].trim()
        writer.println("    class $className($fields) : $baseName() {")
        writer.println("        override fun <R> accept(visitor: Visitor<R>) = visitor.visit$className$baseName(this)")
        writer.println("    }")
    }

    writer.println("}")
    writer.close()
}

fun defineVisitor(writer: PrintWriter, baseName: String, types: List<String>) {
    writer.println("    interface Visitor<R> {")

    for (type in types) {
        val typeName = type.split("|")[0].trim()
        writer.println("        fun visit$typeName$baseName(${baseName.toLowerCase()}: $typeName): R")
    }

    writer.println("    }")
}

generateCode("../src")