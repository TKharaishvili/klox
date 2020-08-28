class Interpreter : Expr.Visitor<Any?> {
    fun interpret(expression: Expr) = try {
        val value = evaluate(expression)
        println(stringify(value))
    } catch (error: RuntimeError) {
        Lox.runtimeError(error)
    }

    override fun visitBinaryExpr(expr: Expr.Binary): Any? {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)

        return when (expr.operator.type) {
            TokenType.GREATER -> when {
                left is Double && right is Double -> left > right
                else -> throw mustBeNumbers(expr.operator)
            }
            TokenType.GREATER_EQUAL -> when {
                left is Double && right is Double -> left >= right
                else -> throw mustBeNumbers(expr.operator)
            }
            TokenType.LESS -> when {
                left is Double && right is Double -> left < right
                else -> throw mustBeNumbers(expr.operator)
            }
            TokenType.LESS_EQUAL -> when {
                left is Double && right is Double -> left <= right
                else -> throw mustBeNumbers(expr.operator)
            }
            TokenType.MINUS -> when {
                left is Double && right is Double -> left - right
                else -> throw mustBeNumbers(expr.operator)
            }
            TokenType.PLUS -> when {
                left is Double && right is Double -> left + right
                left is String && right is String -> left + right
                else -> throw RuntimeError(expr.operator, "Operands must be two numbers or two strings.")
            }
            TokenType.SLASH -> when {
                left is Double && right is Double -> left / right
                else -> throw mustBeNumbers(expr.operator)
            }
            TokenType.STAR -> when {
                left is Double && right is Double -> left * right
                else -> throw mustBeNumbers(expr.operator)
            }
            TokenType.BANG_EQUAL -> !isEqual(left, right)
            TokenType.EQUAL_EQUAL -> isEqual(left, right)
            // Unreachable
            else -> null
        }
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): Any? {
        return evaluate(expr.expression)
    }

    override fun visitLiteralExpr(expr: Expr.Literal): Any? {
        return expr.value
    }

    override fun visitUnaryExpr(expr: Expr.Unary): Any? {
        val right = evaluate(expr.right)

        return when (expr.operator.type) {
            TokenType.BANG -> !isTruthy(right)
            TokenType.MINUS -> when (right) {
                is Double -> -right
                else -> throw RuntimeError(expr.operator, "Operand must be a number.")
            }
            // Unreachable
            else -> null
        }
    }

    private fun mustBeNumbers(operator: Token) = RuntimeError(operator, "Operands must be numbers.")

    private fun isTruthy(obj: Any?) = when (obj) {
        null -> false
        is Boolean -> obj
        else -> true
    }

    private fun isEqual(a: Any?, b: Any?) = when (a) {
        null -> b == null
        else -> a == b
    }

    private fun stringify(obj: Any?) = when (obj) {
        null -> "nil"
        // Hack. Work around Kotlin adding ".0" to integer-valued doubles.
        is Double -> {
            var text = obj.toString()
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length - 2)
            }
            text
        }
        else -> obj.toString()
    }

    private fun evaluate(expr: Expr) = expr.accept(this)
}