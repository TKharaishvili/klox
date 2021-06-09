import java.io.PrintWriter

fun generateCode(outputDir: String) {
    defineAst(outputDir, "Expr", listOf(
            "Assign | val name: Token, val value: Expr",
            "Binary | val left: Expr, val operator: Token, val right: Expr",
            "Call | val callee: Expr, val paren: Token, val arguments: List<Expr>",
            "Get | val obj: Expr, val name: Token",
            "Grouping | val expression: Expr",
            "Literal | val value: Any?",
            "Logical | val left: Expr, val operator: Token, val right: Expr",
            "Set | val obj: Expr, val name: Token, val value: Expr",
            "Super | val keyword: Token, val method: Token",
            "This | val keyword: Token",
            "Unary | val operator: Token, val right: Expr",
            "Variable | val name: Token"
    ))

    defineAst(outputDir, "Stmt", listOf(
            "Block | val statements: List<Stmt?>",
            "Class | val name: Token, val superclass: Expr.Variable?, val methods: List<Stmt.Function>",
            "Expression | val expression: Expr",
            "Function | val name: Token, val params: List<Token>, val body: List<Stmt?>",
            "If | val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt?",
            "Print | val expression: Expr",
            "Return | val keyword: Token, val value: Expr?",
            "Var | val name: Token, val initializer: Expr?",
            "While | val condition: Expr, val body: Stmt"
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
        writer.println("        fun visit$typeName$baseName(${baseName.lowercase()}: $typeName): R")
    }

    writer.println("    }")
}

generateCode("../src")