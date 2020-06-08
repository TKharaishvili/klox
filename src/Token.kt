class Token(private val type: TokenType,
            private val lexeme: String,
            private val literal: Any?,
            private val line: Int
) {
    override fun toString() = "$type $lexeme $literal"
}