package world.gregs.voidps.engine.data

class Yaml(
    val parser: YamlParserI = YamlParser()
) {

    fun setMap(map: (key: String, value: Any) -> Any) {
        parser.mapModifier = map
    }

    fun setList(list: (value: Any) -> Any) {
        parser.listModifier = list
    }

    fun parse(
        charArray: CharArray,
        length: Int = charArray.size
    ): Any = parser.parse(charArray, length)

}