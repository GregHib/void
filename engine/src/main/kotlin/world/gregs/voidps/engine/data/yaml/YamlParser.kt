package world.gregs.voidps.engine.data.yaml

import world.gregs.voidps.engine.data.yaml.config.CollectionConfiguration
import world.gregs.voidps.engine.data.yaml.parse.ExplicitParser
import world.gregs.voidps.engine.data.yaml.parse.NormalParser
import java.io.File

/**
 * High performance parser for simplified YAML
 */
class YamlParser(
    private val defaultConfig: CollectionConfiguration = CollectionConfiguration(),
    val reader: CharReader = CharReader(defaultConfig.createMap()),
    private val explicit: ExplicitParser = ExplicitParser(reader, defaultConfig),
    private val normal: NormalParser = NormalParser(reader, defaultConfig, explicit)
) {

    var config: CollectionConfiguration = defaultConfig

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> load(path: String, config: CollectionConfiguration = defaultConfig, fileSize: Int = 3_000_000): T {
        this.config = config
        val charArray = CharArray(fileSize)
        val fr = File(path).reader()
        val length = fr.read(charArray)
        return parse(charArray, length) as T
    }

    fun parse(string: String) = parse(string.toCharArray())

    fun parse(charArray: CharArray, length: Int = charArray.size): Any {
        explicit.config = config
        normal.config = config
        reader.anchors.clear()
        reader.set(charArray, length)
        reader.nextLine()
        val output = normal.value(indentOffset = 0, withinMap = false)
        config = defaultConfig
        return output
    }
}