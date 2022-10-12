package world.gregs.voidps.engine.utility

import net.pearx.kasechange.formatter.CaseFormatterConfig
import net.pearx.kasechange.splitter.WordSplitter
import net.pearx.kasechange.toCase
import net.pearx.kasechange.universalWordSplitter
import java.text.DecimalFormat

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

fun String.plural(count: Long = 2L, plural: String = "s"): String {
    return if (count == 1L) this else suffixIfNot(plural)
}

fun String.suffixIfNot(suffix: String): String {
    return if (endsWith(suffix)) this else "${this}$suffix"
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

private val dec = DecimalFormat("#,###")

fun Long.toDigitGroupString(): String {
    return dec.format(this)
}

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

fun Int.nearby(size: Int): IntRange {
    return this - size..this + size
}

private val capitaliseFormat = CaseFormatterConfig(false, " ", wordCapitalize = false, firstWordCapitalize = true)

fun String.toSentenceCase(from: WordSplitter = universalWordSplitter()): String = toCase(capitaliseFormat, from)
