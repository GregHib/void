package rs.dusk.utility.func

import org.jetbrains.kotlin.util.suffixIfNot

fun String.plural(count: Int, plural: String = "s") = plural(count.toLong(), plural)

fun String.plural(count: Long, plural: String = "s"): String {
    return if (count == 1L) this else suffixIfNot(plural)
}

fun Boolean?.toInt() = if (this == true) 1 else 0

fun Int?.toBoolean() = this == 1

fun Int.nearby(size: Int): IntRange {
    return this - size..this + size
}

fun String?.toUnderscoreCase(): String {
    var formatted = this
    formatted = formatted?.replace(" ", "_")
    return formatted?.toLowerCase() ?: ""
}

fun String?.toCamelCase(): String {
    if (this == null) {
        return ""
    }
    val formatted = StringBuilder()
    var newWord = true
    for (char in toLowerCase().replace("_", " ")) {
        formatted.append(
            if (newWord) {
                newWord = false
                char.toUpperCase()
            } else {
                char
            }
        )
        if (char == ' ') {
            newWord = true
        }
    }
    return formatted.toString()
}

fun String?.toPascalCase(): String {
    if (this == null) {
        return ""
    }
    val formatted = StringBuilder()
    val text = replace("_", " ").toLowerCase()
    var word = false
    for (i in indices) {
        val char = text[i]
        formatted.append(
            if (i == 0 || word) {
                word = false
                char.toUpperCase()
            } else {
                char
            }
        )
        if (char == ' ') {
            word = true
        }
    }
    return formatted.toString()
}