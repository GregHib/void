package world.gregs.voidps.utility.func

import org.jetbrains.kotlin.util.suffixIfNot

fun String.plural(count: Int, plural: String = "s") = plural(count.toLong(), plural)

fun String.plural(count: Long, plural: String = "s"): String {
    return if (count == 1L) this else suffixIfNot(plural)
}

/*
    International System of Units (SI)
 */
fun Long.toSIPrefix(): String {
    return when {
        this >= 0xe8d4a51000 -> "${this / 0xe8d4a51000}T"
        this >= 0x3b9aCa00 -> "${this / 0x3b9aCa00}B"
        this >= 0xf4240 -> "${this / 0xf4240}M"
        this >= 0x3e8 -> "${this / 0x3e8}K"
        else -> toString()
    }
}

fun Int.toSIPrefix() = toLong().toSIPrefix()

fun String.toSILong(): Long {
    val last = last()
    return if (last.isLetter()) {
        val long = removeSuffix(last.toString()).toLongOrNull() ?: return 0L
        when (last().toLowerCase()) {
            't' -> long * 0xe8d4a51000
            'b' -> long * 0x3b9aCa00
            'm' -> long * 0xf4240
            'k' -> long * 0x3e8
            else -> long
        }
    } else {
        toLongOrNull() ?: 0L
    }
}

fun String.toSIInt() = toSILong().toInt()

fun Boolean?.toInt() = if (this == true) 1 else 0

fun Int?.toBoolean() = this == 1

fun Int.nearby(size: Int): IntRange {
    return this - size..this + size
}

/**
 * PascalCase123 or underscore_case to Title Case 123
 */
fun String.toTitleCase(): String {
    val formatted = StringBuilder()
    var first = true
    var isInt = true
    for (char in replace("_", " ")) {
        if (!first && (char.isUpperCase() || (char.isDigit() && !isInt))) {
            formatted.append(" ")
            isInt = true
        } else if(!char.isDigit()) {
            isInt = false
        }
        formatted.append(char)
        first = false
    }
    return formatted.toString()
}

/**
 * Title Case to underscore_case
 */
fun String?.toUnderscoreCase(): String {
    return this?.replace(" ", "_")?.toLowerCase() ?: ""
}

/**
 * underscore_case or Title Case to camelCase
 */
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

/**
 * underscore_case or Title Case to PascalCase
 */
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