package world.gregs.voidps.engine.data.yaml

import world.gregs.voidps.engine.data.CharArrayReader

open class ExplicitParser(
    reader: CharArrayReader,
    val collection: CollectionFactory
) : ValueParser(reader) {

    override fun isClosingTerminator(char: Char) = super.isClosingTerminator(char) || char == '}' || char == ']' || char == ','

    override fun isOpeningTerminator(char: Char) = super.isOpeningTerminator(char) || char == '{' || char == '['

    override fun parseCollection(indentOffset: Int, withinMap: Boolean): Any {
        val value = parseType()
        return if (reader.inBounds && reader.char == ':') {
            keyValuePair(value.toString())
        } else {
            value
        }
    }

    private fun keyValuePair(key: String): Map<String, Any> {
        val map = collection.createMap()
        reader.skip() // skip colon
        reader.skipSpaces()
        if (reader.outBounds) {
            collection.setEmptyMapValue(map, key)
            return map
        }
        val currentIndent = reader.indentation
        if (reader.isLineEnd()) {
            reader.nextLine()
            if (reader.indentation < currentIndent) {
                collection.setEmptyMapValue(map, key)
                return map
            } else if (reader.indentation == currentIndent && !reader.isListItem()) {
                collection.setEmptyMapValue(map, key)
            } else {
                collection.setMapValue(this, map, key, 0, true)
            }
        } else {
            collection.setMapValue(this, map, key, 0, true)
        }
        return map
    }

    override fun parseExplicitMap(): Map<String, Any> {
        val map = collection.createMap()
        reader.skip() // skip opening char
        reader.nextLine()
        while (reader.inBounds) {
            val key = parseType().toString()
            if (reader.inBounds && reader.char != ':') {
                throw IllegalArgumentException("Expected key-pair value ${reader.exception}")
            }
            reader.skip() // skip colon
            reader.nextLine()
            collection.setMapValue(this, map, key, 0, false)
            reader.nextLine()
            val char = reader.char
            reader.skip()// skip comma/closing char
            reader.nextLine()
            when (char) {
                ',' -> {
                }
                '}' -> return map
                else -> throw IllegalArgumentException("Expecting key-value pair or end of map ${reader.exception}")
            }
        }
        return map
    }

    override fun parseExplicitList(): List<Any> {
        val list = collection.createList()
        reader.skip() // skip opening char
        reader.nextLine()
        while (reader.inBounds) {
            collection.addListItem(this, list, 0, false)
            reader.nextLine()
            val char = reader.char
            reader.skip() // skip comma / closing char
            reader.nextLine()
            when (char) {
                ',' -> {

                }
                ']' -> return list
                else -> throw IllegalArgumentException("Expecting item or end of list ${reader.exception}")
            }
        }
        return list
    }

}