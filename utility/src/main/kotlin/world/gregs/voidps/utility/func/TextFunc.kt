package world.gregs.voidps.utility.func

import org.jetbrains.kotlin.util.suffixIfNot
import java.text.DecimalFormat

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

val dec = DecimalFormat("#,###")

fun Long.toDigitGroupString(): String {
    return dec.format(this)
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
 * Converts string to Title Case - All words are [capitalize]d with spaces between words and digits
 */
fun String.toTitleCase(): String {
    val first = 0.toChar()
    var previous = first
    return buildString {
        for (char in this@toTitleCase) {
            append(when {
                char == '_' -> ' '
                previous == first && !char.isDigit() && char.isLowerCase() -> char.toUpperCase()
                previous != first && char.isLowerCase() && (previous == '_' || previous == ' ') -> char.toUpperCase()
                previous != first && previous != '_' && previous != ' ' && (
                        char.isUpperCase() && !previous.isUpperCase() ||
                                char.isDigit() && !previous.isDigit() ||
                                (!char.isDigit() && char != '_' && char != ' ' && previous.isDigit())
                        ) -> {
                    append(' ')
                    char.toUpperCase()
                }
                else -> char
            })
            previous = char
        }
    }
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
 * Converts string to PascalCase - words are separated by capital letters, no spaces or underscores
 * @param digitise whether lower case letters immediately following digits should be capitalised
 */
fun String?.toPascalCase(digitise: Boolean = true): String {
    if (this == null) {
        return ""
    }
    val first = 0.toChar()
    var previous = first
    return buildString {
        for (char in this@toPascalCase) {
            append(when {
                char == ' ' || char == '_' -> {
                    previous = char
                    continue
                }
                (previous == first || previous == ' ' || previous == '_' || (digitise && previous.isDigit())) && char.isLowerCase() -> char.toUpperCase()
                else -> char
            })
            previous = char
        }
    }
}