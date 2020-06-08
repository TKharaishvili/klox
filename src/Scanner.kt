class Scanner {
    private val source: String
    private val tokens = mutableListOf<Token>()
    private var start = 0;
    private var current = 0;
    private var line = 1;
    private val digitRange = '0'..'9'

    constructor(source: String) {
        this.source = source
    }

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }
        tokens.add(Token(TokenType.EOF, "", null, line))
        return tokens
    }

    private fun isAtEnd() = current >= source.length

    private fun scanToken() {
        val c = advance()
        when (c) {
            '(' -> addToken(TokenType.LEFT_PAREN)
            ')' -> addToken(TokenType.RIGHT_PAREN)
            '{' -> addToken(TokenType.LEFT_BRACE)
            '}' -> addToken(TokenType.RIGHT_BRACE)
            ',' -> addToken(TokenType.COMMA)
            '.' -> addToken(TokenType.DOT)
            '-' -> addToken(TokenType.MINUS)
            '+' -> addToken(TokenType.PLUS)
            ';' -> addToken(TokenType.SEMICOLON)
            '*' -> addToken(TokenType.STAR)
            '!' -> addToken(if (match('=')) TokenType.BANG_EQUAL else TokenType.BANG)
            '=' -> addToken(if (match('=')) TokenType.EQUAL_EQUAL else TokenType.EQUAL)
            '<' -> addToken(if (match('=')) TokenType.LESS_EQUAL else TokenType.LESS)
            '>' -> addToken(if (match('=')) TokenType.GREATER_EQUAL else TokenType.GREATER)
            '/' -> {
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance()
                } else {
                    addToken(TokenType.SLASH)
                }
            }
            ' ', '\r', '\t' -> {}
            '\n' -> line++
            '"' -> string()
            in digitRange -> number()
            else -> {
                if (c.isAlpha()) {
                    identifier()
                } else {
                    error(line, "Unexpected character.")
                }
            }
        }
    }

    private fun identifier() {
        val c = peek()
        while (c.isAlpha() || c in digitRange) advance()

        // See if the identifier is a reserved word
        val text = source.substring(start, current)
        val type = keywords[text] ?: TokenType.IDENTIFIER

        addToken(type)
    }

    private fun number() {
        while (peek() in digitRange) advance()

        // Look for a fractional part
        if (peek() == '.' && peekNext() in digitRange) {
            //Consume the .
            advance()

            while (peek() in digitRange) advance()
        }

        addToken(TokenType.NUMBER, source.substring(start, current).toDouble())
    }

    private fun string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++
            advance()
        }

        // Unterminated string
        if (isAtEnd()) {
            error(line, "Unterminated string.")
            return
        }

        // The closing "
        advance()

        // Trim the surrounding quotes.
        val value = source.substring(start + 1, current - 1)
        addToken(TokenType.STRING, value)
    }

    private fun advance(): Char {
        current++
        return source[current - 1]
    }

    private fun match(expected: Char): Boolean {
        if (isAtEnd()) return false
        if (source[current] != expected) return false

        current++
        return true
    }

    private fun peek() = if (isAtEnd()) Char.MIN_VALUE else source[current]

    private fun peekNext() = if (current + 1 > source.length) Char.MIN_VALUE else source[current + 1]

    private fun Char.isAlpha() = this in 'a'..'z' || this in 'A'..'Z' || this == '_'

    private fun addToken(type: TokenType) {
        addToken(type, null)
    }

    private fun addToken(type: TokenType, literal: Any?) {
        val text = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }

    companion object {
        val keywords = mapOf(
            "and" to TokenType.AND,
            "class" to TokenType.CLASS,
            "and" to TokenType.AND,
            "class" to TokenType.CLASS,
            "else" to TokenType.ELSE,
            "false" to TokenType.FALSE,
            "for" to TokenType.FOR,
            "fun" to TokenType.FUN,
            "if" to TokenType.IF,
            "nil" to TokenType.NIL,
            "or" to TokenType.OR,
            "print" to TokenType.PRINT,
            "return" to TokenType.RETURN,
            "super" to TokenType.SUPER,
            "this" to TokenType.THIS,
            "true" to TokenType.TRUE,
            "var" to TokenType.VAR,
            "while" to TokenType.WHILE
        )
    }
}