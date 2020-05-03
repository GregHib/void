package rs.dusk.utility.func

import org.jetbrains.kotlin.util.suffixIfNot
import java.util.*

class TextFunc {

    companion object {
        private val VALID_CHARS = charArrayOf(
            '_',
            'a',
            'b',
            'c',
            'd',
            'e',
            'f',
            'g',
            'h',
            'i',
            'j',
            'k',
            'l',
            'm',
            'n',
            'o',
            'p',
            'q',
            'r',
            's',
            't',
            'u',
            'v',
            'w',
            'x',
            'y',
            'z',
            '0',
            '1',
            '2',
            '3',
            '4',
            '5',
            '6',
            '7',
            '8',
            '9'
        )

        fun invalidAccountName(name: String): Boolean {
            return name.length < 2 || name.length > 12 || name.startsWith("_") || name.endsWith("_") || name.contains("__") || containsInvalidCharacter(
                name
            )
        }

        fun containsInvalidCharacter(name: String): Boolean {
            for (c in name.toCharArray()) {
                if (containsInvalidCharacter(c)) {
                    return true
                }
            }
            return false
        }

        private fun containsInvalidCharacter(c: Char): Boolean {
            for (vc in VALID_CHARS) {
                if (vc == c) {
                    return false
                }
            }
            return true
        }

        fun IPAddressToNumber(ipAddress: String?): Int {
            val st = StringTokenizer(ipAddress, ".")
            val ip = IntArray(4)
            var i = 0
            while (st.hasMoreTokens()) {
                ip[i++] = st.nextToken().toInt()
            }
            return ip[0] shl 24 or (ip[1] shl 16) or (ip[2] shl 8) or ip[3]
        }

    }
}

fun String.plural(count: Int, plural: String = "s"): String {
    return if (count == 1) this else suffixIfNot(plural)
}

fun Boolean?.toInt() = if (this == true) 1 else 0
fun Int?.toBoolean() = this == 1

/**
 * Formats a name for protocol; all lowercase, replaces all spaces with underscores
 * @return The formatted name
 */
fun String?.protocolFormat(): String {
    var formatted = this
    formatted = formatted?.replace(" ", "_")
    return formatted?.toLowerCase() ?: ""
}

/**
 * Formats a players username for display, uppercase on only beginning of new words
 * @return The formatted name
 */
fun String?.displayFormat(): String {
    if (this == null) {
        return ""
    }
    val formatted = StringBuilder()
    var newWord = true
    for (char in this.toLowerCase().replace("_", " ")) {
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