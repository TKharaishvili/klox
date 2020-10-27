import java.lang.RuntimeException

class Parser(private val tokens: List<Token>) {
    private class ParseError: RuntimeException()

    private var current = 0

    fun parse(): List<Stmt?> {
        val statements = mutableListOf<Stmt?>()
        while (!isAtEnd()) {
            statements.add(declaration())
        }
        return statements
    }

    private fun expression() = assignment()

    private fun declaration() = try {
        if (match(TokenType.VAR)) varDeclaration() else statement()
    } catch (error: ParseError) {
        synchronize()
        null
    }

    private fun statement() = when {
        match(TokenType.PRINT) -> printStatement()
        match(TokenType.LEFT_BRACE) -> Stmt.Block(block())
        else -> expressionStatement()
    }

    private fun printStatement(): Stmt {
        val value = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after value.")
        return Stmt.Print(value)
    }

    private fun varDeclaration(): Stmt {
        val name = consume(TokenType.IDENTIFIER, "Expect variable name.")
        val initializer = if (match(TokenType.EQUAL)) expression() else null
        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.")
        return Stmt.Var(name, initializer)
    }

    private fun expressionStatement(): Stmt {
        val expr = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after expression.")
        return Stmt.Expression(expr)
    }

    private fun block(): List<Stmt?> {
        val statements = mutableListOf<Stmt?>()

        while (!TokenType.RIGHT_BRACE.check() && !isAtEnd()) {
            statements.add(declaration())
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.")
        return statements
    }

    private fun assignment(): Expr {
        val expr = equality()

        if (match(TokenType.EQUAL)) {
            val equals = previous()
            val value = assignment()

            if (expr is Expr.Variable) {
                return Expr.Assign(expr.name, value)
            }
            error(equals, "Invalid assignment target.")
        }
        return expr
    }

    private fun equality() = binary(::comparison, TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)

    private fun comparison() = binary(::addition, TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)

    private fun addition() = binary(::multiplication, TokenType.MINUS, TokenType.PLUS)

    private fun multiplication() = binary(::unary, TokenType.SLASH, TokenType.STAR)

    private fun binary(operation: () -> Expr, vararg types: TokenType): Expr {
        var expr = operation()

        while (match(*types)) {
            val operator = previous()
            val right = operation()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    private fun unary(): Expr {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            val operator = previous()
            val right = unary()
            return Expr.Unary(operator, right)
        }
        return primary()
    }

    private fun primary() = when {
        match(TokenType.FALSE) -> Expr.Literal(false)
        match(TokenType.TRUE) -> Expr.Literal(true)
        match(TokenType.NIL) -> Expr.Literal(null)
        match(TokenType.NUMBER, TokenType.STRING) -> Expr.Literal(previous().literal)
        match(TokenType.IDENTIFIER) -> Expr.Variable(previous())
        match(TokenType.LEFT_PAREN) -> {
            val expr = expression()
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.")
            Expr.Grouping(expr)
        }
        else -> throw error(peek(), "Expect expression.")
    }

    private fun match(vararg types: TokenType): Boolean {
        if (types.any { it.check() }) {
            advance()
            return true
        }
        return false
    }

    private fun consume(type: TokenType, message: String): Token {
        if (type.check()) return advance()
        throw error(peek(), message)
    }

    private fun TokenType.check(): Boolean {
        if (isAtEnd()) return false
        return peek().type == this
    }

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd() = peek().type == TokenType.EOF

    private fun peek() = tokens[current]

    private fun previous() = tokens[current - 1]

    private fun error(token: Token, message: String): ParseError {
        Lox.error(token, message)
        return ParseError()
    }

    private fun synchronize() {
        advance()
        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON)
                return

            when (peek().type) {
                TokenType.CLASS,
                TokenType.FUN,
                TokenType.VAR,
                TokenType.FOR,
                TokenType.IF,
                TokenType.WHILE,
                TokenType.PRINT,
                TokenType.RETURN ->
                    return
            }
        }
        advance()
    }
}