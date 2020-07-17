class Token(private val type: TokenType,
            public val lexeme: String,
            private val literal: Any?,
            private val line: Int
) {
    override fun toString() = "$type $lexeme $literal"
}