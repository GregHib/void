package world.gregs.voidps.web.site

/**
 * Syntax-highlighting for fenced code blocks in the docs. Deliberately not a general-purpose
 * lexer: each language is a small, ordered list of regexes tried at the current position
 * ([RegexHighlighter]), which is enough for the config and snippet examples in the `.claude`
 * markdown docs without pulling in a JS highlighting library. Add a language by adding a
 * [Highlighter] and registering it in [Highlighters.byAlias].
 */
enum class TokenType(val css: String) {
    KEYWORD("kw"),
    STRING("str"),
    NUMBER("num"),
    COMMENT("cmt"),
    PROPERTY("prop"),
    TYPE("type"),
    BOOLEAN("bool"),
    FUNCTION("fn"),
    ANNOTATION("anno"),
    SECTION("sec"),
    PUNCTUATION("pun"),
    OPERATOR("op"),
}

data class Token(val text: String, val type: TokenType?)

fun interface Highlighter {
    fun tokenize(code: String): List<Token>
}

/** Tries [rules] in order at each position; the first pattern that matches at that exact spot wins. */
class RegexHighlighter(private val rules: List<Pair<TokenType, Regex>>) : Highlighter {
    override fun tokenize(code: String): List<Token> {
        val tokens = mutableListOf<Token>()
        val plain = StringBuilder()
        fun flushPlain() {
            if (plain.isNotEmpty()) {
                tokens += Token(plain.toString(), null)
                plain.clear()
            }
        }

        var i = 0
        while (i < code.length) {
            val match = rules.firstNotNullOfOrNull { (type, pattern) ->
                pattern.matchAt(code, i)?.takeIf { it.value.isNotEmpty() }?.let { type to it }
            }
            if (match != null) {
                flushPlain()
                tokens += Token(match.second.value, match.first)
                i += match.second.value.length
            } else {
                plain.append(code[i])
                i++
            }
        }
        flushPlain()
        return tokens
    }
}

object Highlighters {

    private val json = RegexHighlighter(
        listOf(
            TokenType.PROPERTY to Regex("\"(?:\\\\.|[^\"\\\\])*\"(?=\\s*:)"),
            TokenType.STRING to Regex("\"(?:\\\\.|[^\"\\\\])*\""),
            TokenType.NUMBER to Regex("-?\\b\\d+(?:\\.\\d+)?(?:[eE][+-]?\\d+)?\\b"),
            TokenType.BOOLEAN to Regex("\\b(?:true|false|null)\\b"),
            TokenType.PUNCTUATION to Regex("[{}\\[\\]:,]"),
        ),
    )

    private val toml = RegexHighlighter(
        listOf(
            TokenType.COMMENT to Regex("#[^\n]*"),
            TokenType.SECTION to Regex("\\[[^]\n]*]"),
            TokenType.PROPERTY to Regex("[A-Za-z0-9_.-]+(?=\\s*=)"),
            TokenType.STRING to Regex("\"(?:\\\\.|[^\"\\\\])*\""),
            TokenType.BOOLEAN to Regex("\\b(?:true|false)\\b"),
            TokenType.NUMBER to Regex("-?\\b\\d+(?:\\.\\d+)?\\b"),
            TokenType.OPERATOR to Regex("="),
        ),
    )

    private val ini = RegexHighlighter(
        listOf(
            TokenType.COMMENT to Regex("[;#][^\n]*"),
            TokenType.SECTION to Regex("\\[[^]\n]*]"),
            TokenType.PROPERTY to Regex("[A-Za-z0-9_.-]+(?=\\s*=)"),
            TokenType.STRING to Regex("\"(?:\\\\.|[^\"\\\\])*\""),
            TokenType.NUMBER to Regex("-?\\b\\d+(?:\\.\\d+)?\\b"),
            TokenType.OPERATOR to Regex("="),
        ),
    )

    private val kotlinKeywords = listOf(
        "fun", "val", "var", "class", "object", "interface", "if", "else", "when", "for", "while", "do", "return",
        "import", "package", "private", "public", "protected", "internal", "override", "companion", "data", "enum",
        "sealed", "abstract", "open", "final", "const", "in", "is", "as", "out", "this", "super", "try", "catch",
        "finally", "throw", "suspend", "inline", "noinline", "crossinline", "typealias", "init", "constructor",
        "by", "get", "set", "vararg", "reified", "operator", "infix", "tailrec", "external", "annotation",
        "lateinit", "where", "dynamic",
    )

    private val kotlinKeywordPattern = Regex("\\b(?:" + kotlinKeywords.joinToString("|") + ")\\b")
    private val tripleQuote = "\"".repeat(3)
    private val kotlinTripleQuotedString = Regex(Regex.escape(tripleQuote) + "[\\s\\S]*?" + Regex.escape(tripleQuote))

    private val kotlin = RegexHighlighter(
        listOf(
            TokenType.COMMENT to Regex("/\\*[\\s\\S]*?\\*/"),
            TokenType.COMMENT to Regex("//[^\n]*"),
            TokenType.STRING to kotlinTripleQuotedString,
            TokenType.STRING to Regex("\"(?:\\\\.|[^\"\\\\])*\""),
            TokenType.STRING to Regex("'(?:\\\\.|[^'\\\\])'"),
            TokenType.ANNOTATION to Regex("@[A-Za-z_][A-Za-z0-9_]*"),
            TokenType.KEYWORD to kotlinKeywordPattern,
            TokenType.BOOLEAN to Regex("\\b(?:true|false|null)\\b"),
            TokenType.NUMBER to Regex("\\b0[xX][0-9a-fA-F]+\\b|\\b\\d+(?:\\.\\d+)?[fFLuU]?\\b"),
            TokenType.TYPE to Regex("\\b[A-Z][A-Za-z0-9_]*\\b"),
            TokenType.FUNCTION to Regex("\\b[a-z_][A-Za-z0-9_]*(?=\\()"),
            TokenType.PUNCTUATION to Regex("[{}()\\[\\].,;:]"),
            TokenType.OPERATOR to Regex("[=+\\-*/%<>!&|?]+"),
        ),
    )

    private val byAlias: Map<String, Highlighter> = mapOf(
        "json" to json,
        "toml" to toml,
        "groml" to toml,
        "ini" to ini,
        "kotlin" to kotlin,
        "kt" to kotlin,
    )

    fun forLanguage(language: String?): Highlighter? = language?.lowercase()?.let { byAlias[it] }
}

fun escapeHtml(text: String): String = text
    .replace("&", "&amp;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")
    .replace("\"", "&quot;")

/** Renders [code] as highlighted, HTML-escaped markup; falls back to plain escaped text for unknown languages. */
fun highlightHtml(language: String?, code: String): String {
    val highlighter = Highlighters.forLanguage(language) ?: return escapeHtml(code)
    return buildString {
        for (token in highlighter.tokenize(code)) {
            val escaped = escapeHtml(token.text)
            if (token.type != null) {
                append("<span class=\"tok-").append(token.type.css).append("\">").append(escaped).append("</span>")
            } else {
                append(escaped)
            }
        }
    }
}
