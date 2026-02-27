package world.gregs.voidps.cache.definition.data

import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Extra

data class FontDefinition(
    override var id: Int = -1,
    var glyphWidths: ByteArray = byteArrayOf(),
    var kerningAdjustments: Array<ByteArray>? = null,
    var verticalSpacing: Int = 0,
    var topPadding: Int = 0,
    var bottomPadding: Int = 0,
    override var stringId: String = "",
    override var extras: Map<String, Any>? = null,
) : Definition,
    Extra {

    private fun glyphWidth(glyph: Int): Int = glyphWidths[glyph].toInt() and 0xff

    fun textWidth(input: String, icons: Array<IndexedSprite>? = null): Int {
        var tagStart = -1
        var lastChar = -1
        var totalWidth = 0
        for (index in input.indices) {
            var current = input[index]
            if (current.code == 60) {
                tagStart = index
                continue
            }
            if (current.code == 62 && tagStart != -1) {
                val tag = input.substring(tagStart + 1, index)
                tagStart = -1
                val tagChar = htmlEntityToChar(tag)
                if (tagChar == null) {
                    totalWidth += spriteWidth(tag, icons) ?: continue
                    lastChar = -1
                    continue
                }
                current = tagChar
            }
            if (tagStart == -1) {
                totalWidth += glyphWidths[charToByte(current) and 0xff].toInt() and 0xff
                if (kerningAdjustments != null && lastChar != -1) {
                    totalWidth += kerningAdjustments!![lastChar][current.code].toInt()
                }
                lastChar = current.code
            }
        }
        return totalWidth
    }

    fun truncateText(input: String, maxWidth: Int, icons: Array<IndexedSprite>? = emptyArray()): String {
        var maximumWidth = maxWidth
        if (maximumWidth >= textWidth(input, icons)) {
            return input
        }
        maximumWidth -= textWidth("...", null)
        var tagStart = -1
        var lastChar = -1
        var totalWidth = 0
        var prefix = ""
        for (index in input.indices) {
            var current = input[index]
            if (current.code == 60) {
                tagStart = index
                continue
            }
            if (current.code == 62 && tagStart != -1) {
                val tag = input.substring(tagStart - -1, index)
                tagStart = -1
                val tagChar = htmlEntityToChar(tag)
                if (tagChar == null) {
                    totalWidth += spriteWidth(tag, icons) ?: continue
                    lastChar = -1
                    if (totalWidth > maximumWidth) {
                        return "$prefix..."
                    }
                    prefix = input.substring(0, index - -1)
                    continue
                }
                current = tagChar
            }
            if (tagStart == -1) {
                totalWidth += 0xff and glyphWidths[charToByte(current) and 0xff].toInt()
                if (kerningAdjustments != null && lastChar != -1) {
                    totalWidth += kerningAdjustments!![lastChar][current.code].toInt()
                }
                lastChar = current.code
                var total = totalWidth
                if (kerningAdjustments != null) {
                    total += kerningAdjustments!![current.code][46].toInt()
                }
                if (total > maximumWidth) {
                    return "$prefix..."
                }
                prefix = input.substring(0, index + 1)
            }
        }
        return input
    }

    fun splitLines(input: String, width: Int, icons: Array<IndexedSprite>? = null) = splitLines(input, intArrayOf(width), icons)

    fun width(text: String): Int {
        var openBracket = -1
        var prev = -1
        var width = 0
        val len = text.length
        for (i in 0..<len) {
            var curr = text[i]
            if (curr == '<') {
                openBracket = i
            } else {
                if (curr == '>' && openBracket != -1) {
                    val escaped = text.substring(openBracket + 1, i)
                    openBracket = -1
                    curr = when (escaped) {
                        "lt" -> '<'
                        "gt" -> '>'
                        "nbsp" -> ' '
                        "shy" -> '­'
                        "times" -> '×'
                        "euro" -> '€'
                        "copy" -> '©'
                        "reg" -> '®'
                        else -> continue
                    }
                }

                if (openBracket == -1) {
                    width += this.glyphWidths[charToByte(curr) and 0xFF].toInt() and 0xFF
                    if (this.kerningAdjustments != null && prev != -1) {
                        width += this.kerningAdjustments!![prev][curr.code].toInt()
                    }
                    prev = curr.code
                }
            }
        }
        return width
    }

    fun splitLines(input: String, widths: IntArray? = null, icons: Array<IndexedSprite>? = null): List<String> {
        val output = mutableListOf<String>()
        var totalWidth = 0
        var lineStart = 0
        var lineLength = -1
        var wordWidth = 0
        var wordStart = 0
        var tagStart = -1
        var lastChar = -1
        var colour: String? = null
        for (index in input.indices) {
            var current: Int = charToByte(input[index]) and 0xff
            var extraWidth = 0
            if (current == 60) {
                tagStart = index
                continue
            }
            var currentWidth: Int
            if (tagStart == -1) {
                extraWidth += glyphWidth(current)
                currentWidth = index
                if (kerningAdjustments != null && lastChar != -1) {
                    extraWidth += kerningAdjustments!![lastChar][current].toInt()
                }
                lastChar = current
            } else {
                if (current != 62) {
                    continue
                }
                currentWidth = tagStart
                val tag = input.substring(1 + tagStart, index)
                tagStart = -1
                if (tag == "br") {
                    output.add(input.substring(lineStart, index - -1))
                    lineStart = index + 1
                    lastChar = -1
                    lineLength = -1
                    totalWidth = 0
                    continue
                }
                fun addKernelWidth(glyph: Int) {
                    extraWidth += glyphWidth(glyph)
                    if (kerningAdjustments != null && lastChar != -1) {
                        extraWidth += kerningAdjustments!![lastChar][glyph].toInt()
                    }
                    lastChar = glyph
                }
                when (tag) {
                    "lt" -> addKernelWidth(60)
                    "gt" -> addKernelWidth(62)
                    "nbsp" -> addKernelWidth(160)
                    "shy" -> addKernelWidth(173)
                    "times" -> addKernelWidth(215)
                    "euro" -> addKernelWidth(8364)
                    "copy" -> addKernelWidth(169)
                    "reg" -> addKernelWidth(174)
                    "blue", "orange", "green", "red", "red_orange", "yellow", "lime", "gold", "white",
                    "black", "navy", "maroon", "purple", "brown", "violet", "dark_green", "dark_red",
                        -> colour = tag
                    else -> if (tag.startsWith("col=")) {
                        colour = tag
                    } else {
                        totalWidth += spriteWidth(tag, icons) ?: continue
                        lastChar = -1
                    }
                }
                current = -1
            }
            if (extraWidth <= 0) {
                continue
            }
            totalWidth += extraWidth
            if (widths == null) {
                continue
            }
            if (current == 32) {
                wordStart = 1
                wordWidth = totalWidth
                lineLength = index
            }
            if (totalWidth > widths[if (widths.size > output.size) output.size else widths.size - 1]) {
                if (lineLength >= 0) {
                    if (colour != null && output.isNotEmpty()) {
                        output.add("<${colour}>${input.substring(lineStart, lineLength + 1 - wordStart)}")
                    } else {
                        output.add(input.substring(lineStart, lineLength + 1 - wordStart))
                    }
                    lineStart = lineLength + 1
                    lastChar = -1
                    lineLength = -1
                    totalWidth -= wordWidth
                } else {
                    if (colour != null && output.isNotEmpty()) {
                        output.add("<${colour}>${input.substring(lineStart, currentWidth)}")
                    } else {
                        output.add(input.substring(lineStart, currentWidth))
                    }
                    lineStart = currentWidth
                    lastChar = -1
                    lineLength = -1
                    totalWidth = extraWidth
                }
            }
            if (current == 45) {
                wordWidth = totalWidth
                lineLength = index
                wordStart = 0
            }
        }
        if (lineStart < input.length) {
            if (colour != null && output.isNotEmpty()) {
                output.add("<${colour}>${input.substring(lineStart)}")
            } else {
                output.add(input.substring(lineStart))
            }
        }
        return output
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FontDefinition

        if (id != other.id) return false
        if (!glyphWidths.contentEquals(other.glyphWidths)) return false
        if (kerningAdjustments != null) {
            if (other.kerningAdjustments == null) return false
            if (!kerningAdjustments.contentDeepEquals(other.kerningAdjustments)) return false
        } else if (other.kerningAdjustments != null) {
            return false
        }
        if (verticalSpacing != other.verticalSpacing) return false
        if (topPadding != other.topPadding) return false
        if (bottomPadding != other.bottomPadding) return false
        if (stringId != other.stringId) return false
        if (extras != other.extras) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + glyphWidths.contentHashCode()
        result = 31 * result + (kerningAdjustments?.contentDeepHashCode() ?: 0)
        result = 31 * result + verticalSpacing
        result = 31 * result + topPadding
        result = 31 * result + bottomPadding
        result = 31 * result + stringId.hashCode()
        result = 31 * result + (extras?.hashCode() ?: 0)
        return result
    }

    companion object {
        private fun spriteWidth(tag: String, icons: Array<IndexedSprite>?): Int? {
            if (!tag.startsWith("img=") || icons == null) {
                return null
            }
            return try {
                val id = parseInt(tag.substring(4))
                icons[id].scaleWidth()
            } catch (exception: Exception) {
                null
            }
        }

        private fun htmlEntityToChar(tag: String): Char? = when (tag) {
            "lt" -> '<'
            "gt" -> '>'
            "nbsp" -> '\u00a0'
            "shy" -> '\u00ad'
            "times" -> '\u00d7'
            "euro" -> '\u20ac'
            "copy" -> '\u00a9'
            "reg" -> '\u00ae'
            else -> null
        }

        private fun parseInt(string: String, radix: Int = 10, positive: Boolean = true): Int {
            require(radix in 2..36) { "Invalid radix: $radix" }
            var negative = false
            var valid = false
            var result = 0
            for (index in string.indices) {
                var digit = string[index].code
                if (index == 0) {
                    if (digit == 45) {
                        negative = true
                        continue
                    }
                    if (digit == 43 && positive) {
                        continue
                    }
                }
                digit -= if (digit in 48..57) {
                    48
                } else if (digit in 65..90) {
                    55
                } else if (digit in 97..122) {
                    87
                } else {
                    throw NumberFormatException()
                }
                if (digit >= radix) {
                    throw NumberFormatException()
                }
                if (negative) {
                    digit = -digit
                }
                val temp = (radix * result) + digit
                if (result != temp / radix) {
                    throw NumberFormatException()
                }
                valid = true
                result = temp
            }
            if (!valid) {
                throw NumberFormatException()
            }
            return result
        }

        private fun charToByte(c: Char): Int {
            if (c.code in 1..127 || c.code in 160..255) {
                return c.code
            }
            return when (c.code) {
                8364 -> -128
                8218 -> -126
                402 -> -125
                8222 -> -124
                8230 -> -123
                8224 -> -122
                8225 -> -121
                710 -> -120
                8240 -> -119
                352 -> -118
                8249 -> -117
                338 -> -116
                381 -> -114
                8216 -> -111
                8217 -> -110
                8220 -> -109
                8221 -> -108
                8226 -> -107
                8211 -> -106
                8212 -> -105
                732 -> -104
                8482 -> -103
                353 -> -102
                8250 -> -101
                339 -> -100
                382 -> -98
                376 -> -97
                else -> 63
            }
        }

        val EMPTY = FontDefinition()
    }
}
