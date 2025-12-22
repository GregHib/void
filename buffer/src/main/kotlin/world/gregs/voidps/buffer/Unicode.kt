package world.gregs.voidps.buffer

object Unicode {
    fun byteToChar(b: Int): Int {
        var i = 0xff and b
        require(i != 0) { "Non cp1252 character 0x" + i.toString(16) + " provided" }
        if (i in 128..159) {
            var char = table[i - 128].code
            if (char == 0) {
                char = 63
            }
            i = char
        }
        return i
    }

    fun charToByte(c: Char) = when (val code = c.code) {
        in 0..127 -> code.toByte().toInt()
        in 160..255 -> code.toByte().toInt()
        else -> {
            val indexInTable = table.indexOfFirst { it.code == code }
            if (indexInTable == -1) {
                throw IllegalArgumentException("Char '$c' (0x${code.toString(16)}) not in CP1252 mapping")
            }
            (128 + indexInTable).toByte().toInt()
        }
    }

    val table = charArrayOf(
        '\u20ac',
        '\u0000',
        '\u201a',
        '\u0192',
        '\u201e',
        '\u2026',
        '\u2020',
        '\u2021',
        '\u02c6',
        '\u2030',
        '\u0160',
        '\u2039',
        '\u0152',
        '\u0000',
        '\u017d',
        '\u0000',
        '\u0000',
        '\u2018',
        '\u2019',
        '\u201c',
        '\u201d',
        '\u2022',
        '\u2013',
        '\u2014',
        '\u02dc',
        '\u2122',
        '\u0161',
        '\u203a',
        '\u0153',
        '\u0000',
        '\u017e',
        '\u0178',
    )
}