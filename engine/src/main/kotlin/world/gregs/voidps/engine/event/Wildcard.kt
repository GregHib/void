package world.gregs.voidps.engine.event

/**
 * Compares [other] against [wildcard] which can contain '*' to match against anything of any length
 * or '#' to match against any single digit.
 */
fun wildcardEquals(wildcard: String, other: String): Boolean {
    if (wildcard == "*") {
        return true
    }
    var wildIndex = 0
    var otherIndex = 0
    var starIndex = -1
    var matchIndex = -1

    while (otherIndex < other.length) {
        when {
            wildIndex < wildcard.length && (wildcard[wildIndex] == '#' && other[otherIndex].isDigit()) -> {
                wildIndex++
                otherIndex++
            }
            wildIndex < wildcard.length && wildcard[wildIndex] == '*' -> {
                starIndex = wildIndex
                matchIndex = otherIndex
                wildIndex++
            }
            wildIndex < wildcard.length && wildcard[wildIndex] == other[otherIndex] -> {
                wildIndex++
                otherIndex++
            }
            starIndex != -1 -> {
                wildIndex = starIndex + 1
                matchIndex++
                otherIndex = matchIndex
            }
            else -> return false
        }
    }

    while (wildIndex < wildcard.length && wildcard[wildIndex] == '*') {
        wildIndex++
    }

    return wildIndex == wildcard.length && otherIndex == other.length
}
