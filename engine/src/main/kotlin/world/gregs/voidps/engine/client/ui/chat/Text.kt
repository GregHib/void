package world.gregs.voidps.engine.client.ui.chat

import java.text.DecimalFormat
import java.util.*
import java.util.regex.Pattern

fun String.toIntRange(inclusive: Boolean = false, separator: String = "-"): IntRange {
    val split = split(separator)
    val first = split.firstOrNull()?.toIntOrNull() ?: 0
    val second = split.lastOrNull()?.toIntOrNull() ?: 0
    return if (inclusive) {
        first..second
    } else {
        first until second
    }
}

fun String.plural(count: Int, plural: String = "s") = plural(count.toLong(), plural)

fun String.plural(count: Long = 2L, plural: String = "s"): String = if (count == 1L) this else suffixIfNot(plural)

fun String.suffixIfNot(suffix: String): String = if (endsWith(suffix)) this else "${this}$suffix"

/*
    International System of Units (SI)
 */
fun Long.toSIPrefix(): String = when {
    this >= 0xe8d4a51000 -> "${this / 0xe8d4a51000}T"
    this >= 0x3b9aCa00 -> "${this / 0x3b9aCa00}B"
    this >= 0xf4240 -> "${this / 0xf4240}M"
    this >= 0x3e8 -> "${this / 0x3e8}K"
    else -> toString()
}

private val dec = DecimalFormat("#,###")

fun Number.toDigitGroupString() = dec.format(this) ?: ""

fun Int.toSIPrefix() = toLong().toSIPrefix()

fun String.toSILong(): Long {
    val last = last()
    return if (last.isLetter()) {
        val long = removeSuffix(last.toString()).toLongOrNull() ?: return 0L
        when (last().lowercaseChar()) {
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

fun Int.nearby(size: Int): IntRange = this - size..this + size

private val yRegex = "^y(b[lor]|cl[ea]|fere|gg|p[ios]|rou|tt).*".toRegex()
private val capitalRegex = "(?!FJO|[HLMNS]Y.|RY[EO]|SQU|(F[LR]?|[HL]|MN?|N|RH?|S[CHKLMNPTVW]?|X(YL)?)[AEIOU])[FHLMNRSX][A-Z]".toRegex()
private val specialRegex = "^U[NK][AIEO].*".toRegex()
private val vowelRegex1 = "^e[uw].*".toRegex()
private val vowelRegex2 = "^onc?e\\b.*".toRegex()
private val vowelRegex3 = "^uni([^nmd]|mo).*".toRegex()
private val vowelRegex4 = "^u[bcfhjkqrst][aeiou].*".toRegex()
private val pattern = Pattern.compile("(\\w+)\\s*.*")

fun String.an(): String {
    if (isEmpty()) {
        return " a"
    }
    if (endsWith('s')) {
        return ""
    }

    // Getting the first word
    val matcher = pattern.matcher(this)
    val word = if (matcher.matches()) matcher.group(1) else return " an"
    val lowercaseWord = word.lowercase(Locale.getDefault())

    // Specific start of words that should be preceded by 'an'
    val altCases = arrayOf("euler", "heir", "honest", "hono")
    for (altCase in altCases) {
        if (lowercaseWord.startsWith(altCase)) {
            return " an"
        }
    }
    if (lowercaseWord.startsWith("hour") && !lowercaseWord.startsWith("houri")) {
        return " an"
    }

    // Single letter word which should be preceded by 'an'
    if (lowercaseWord.length == 1) {
        return if ("aedhilmnorsx".indexOf(lowercaseWord) >= 0) " an" else " a"
    }

    // Capital words which should likely be preceded by 'an'
    if (word.matches(capitalRegex)) {
        return " an"
    }

    // Special cases where a word that begins with a vowel should be preceded by 'a'
    if (lowercaseWord.matches(vowelRegex1)) {
        return " a"
    }
    if (lowercaseWord.matches(vowelRegex2)) {
        return " a"
    }
    if (lowercaseWord.matches(vowelRegex3)) {
        return " a"
    }
    if (lowercaseWord.matches(vowelRegex4)) {
        return " a"
    }

    // Special capital words (UK, UN)
    if (word.matches(specialRegex)) {
        return " a"
    } else if (word === word.uppercase(Locale.getDefault())) {
        return if ("aedhilmnorsx".indexOf(lowercaseWord.substring(0, 1)) >= 0) " an" else " a"
    }

    // Basic method of words that begin with a vowel being preceded by 'an'
    if ("aeiou".indexOf(lowercaseWord.substring(0, 1)) >= 0) {
        return " an"
    }

    // Instances where y followed by specific letters is preceded by 'an'
    return if (lowercaseWord.matches(yRegex)) " an" else " a"
}
