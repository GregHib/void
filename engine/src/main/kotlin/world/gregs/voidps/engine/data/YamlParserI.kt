package world.gregs.voidps.engine.data

interface YamlParserI {
    var mapModifier: (key: String, value: Any) -> Any
    var listModifier: (value: Any) -> Any

    fun parseVal(indentOffset: Int = 0, withinMap: Boolean = false): Any

    fun parse(charArray: CharArray, length: Int = charArray.size): Any
}