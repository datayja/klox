package datayja.klox

class Scanner(
    private val source: String,
) {
    private val tokens: MutableList<Token> = mutableListOf<Token>()
    private var start: UInt = 0u
    private var current: UInt = 0u
    private var line: UInt = 1u

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }

        tokens += Token(TokenType.EOF, "", null, line)
        return tokens
    }

    private fun isAtEnd(): Boolean {
        return current >= source.length.toUInt()
    }

    private fun scanToken() {
        val c: Char = advance()
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

            ' ',
            '\r',
            '\t' -> {}

            '\n' -> { line += 1u }

            '"' -> string()

            else -> {
                if (isDigit(c)) number()
                else error(line, "Unexpected character: $c")
            }
        }
    }

    private fun advance(): Char {
        try {
            return source[current]
        } finally {
            current += 1u
        }
    }

    private fun match(expected: Char): Boolean {
        if (isAtEnd()) return false
        else if (source[current] != expected) return false

        current += 1u
        return true
    }

    private fun peek(): Char {
        return if (isAtEnd()) Char.MIN_VALUE
        else source[current]
    }

    private fun peekNext(): Char {
        return if (current + 1u >= source.length.toUInt()) Char.MIN_VALUE
        else source[current + 1u]
    }

    private fun addToken(type: TokenType) {
        addToken(type, null)
    }

    private fun addToken(type: TokenType, literal: Any?) {
        val text = source.substring(start, current)
        tokens += Token(type, text, literal, line)
    }

    private fun string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line += 1u
            advance()
        }

        if (isAtEnd()) {
            error(line, "Unterminated string.")
            return
        }

        // the closing quote (").
        advance()

        // trim the surrounding quotes
        val value = source.substring(start + 1u, current - 1u)
        addToken(TokenType.STRING, value)
    }

    private fun isDigit(c: Char): Boolean {
        return (c in '0'..'9')
    }

    private fun number() {
        while (isDigit(peek())) advance()

        // look for a fractional part
        if (peek() == '.' && isDigit(peekNext())) {
            advance()

            while (isDigit(peek())) advance()
        }

        addToken(TokenType.NUMBER, source.substring(start, current).toDouble())
    }

    private operator fun String.get(at: UInt): Char {
        return this[at.toInt()]
    }

    private fun String.substring(startIndex: UInt, endIndex: UInt): String {
        return source.substring(startIndex.toInt(), endIndex.toInt())
    }
}
