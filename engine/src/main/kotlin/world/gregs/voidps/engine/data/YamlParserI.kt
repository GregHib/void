package world.gregs.voidps.engine.data

interface YamlParserI {
    fun parseValue(indentOffset: Int = 0, withinMap: Boolean = false): Any

    fun parse(charArray: CharArray, length: Int = charArray.size): Any
}