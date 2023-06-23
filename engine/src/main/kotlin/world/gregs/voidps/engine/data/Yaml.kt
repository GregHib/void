package world.gregs.voidps.engine.data

class Yaml {

    val parser = YamlParser()

    fun parse(
        charArray: CharArray,
        length: Int = charArray.size
    ): Any = parser.parse(charArray, length)

}