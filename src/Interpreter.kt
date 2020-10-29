class Interpreter : Expr.Visitor<Any?>, Stmt.Visitor<Unit> {
    private var environment = Environment()

    fun interpret(statements: List<Stmt?>) = try {
        for (statement in statements) {
            //if the execution reaches this point, the statement can NEVER be null!
            execute(statement!!)
        }
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

    override fun visitLogicalExpr(expr: Expr.Logical): Any? {
        val left = evaluate(expr.left)
        return if (expr.operator.type == TokenType.OR == isTruthy(left))
            left else
            evaluate(expr.right)
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

    override fun visitVariableExpr(expr: Expr.Variable): Any? = environment.get(expr.name)

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

    private fun execute(stmt: Stmt) = stmt.accept(this)

    private fun executeBlock(statements: List<Stmt?>, environment: Environment) {
        val previous = this.environment
        try {
            this.environment = environment

            for (statement in statements) {
                //if the execution reaches this point, the statement can NEVER be null!
                execute(statement!!)
            }
        } finally {
            this.environment = previous
        }
    }

    override fun visitBlockStmt(stmt: Stmt.Block) {
        executeBlock(stmt.statements, Environment(environment))
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        evaluate(stmt.expression)
    }

    override fun visitIfStmt(stmt: Stmt.If) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch)
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch)
        }
    }

    override fun visitPrintStmt(stmt: Stmt.Print) {
        val value = evaluate(stmt.expression)
        println(stringify(value))
    }

    override fun visitVarStmt(stmt: Stmt.Var) {
        val value = if (stmt.initializer != null) evaluate(stmt.initializer) else null
        environment.define(stmt.name.lexeme, value)
    }

    override fun visitWhileStmt(stmt: Stmt.While) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body)
        }
    }

    override fun visitAssignExpr(expr: Expr.Assign): Any? {
        val value = evaluate(expr.value)
        environment.assign(expr.name, value)
        return value
    }
}