package world.gregs.yaml

import it.unimi.dsi.fastutil.chars.CharArrayList

class CharWriter {
    private val array = CharArrayList()

    fun indent(indent: Int) {
        for (i in 0 until indent * 2) {
            array.add(' ')
        }
    }

    fun appendLine() {
        array.add('\n')
    }

    fun append(char: Char) {
        array.add(char)
    }

    fun toCharArray(): CharArray = array.toCharArray()

    fun clear() {
        array.clear()
    }
}
