package world.gregs.voidps.engine.data.yaml

import world.gregs.voidps.engine.data.CharArrayReader

abstract class ValueParser(val reader: CharArrayReader) {

    abstract val explicit: ExplicitParser

    fun parseValue(indentOffset: Int, withinMap: Boolean): Any {
        return when (reader.char) {
            '[' -> explicit.parseExplicitList()
            '{' -> explicit.parseExplicitMap()
            '&' -> {
                reader.skipAnchorString()
                reader.nextLine()
                parseValue(0, false)
            }
            else -> parseCollection(indentOffset, withinMap)
        }
    }

    abstract fun parseCollection(indentOffset: Int, withinMap: Boolean): Any
}