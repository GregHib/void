package world.gregs.yaml

import world.gregs.yaml.config.CollectionConfiguration
import world.gregs.yaml.parse.ExplicitParser
import world.gregs.yaml.parse.NormalParser
import world.gregs.yaml.write.ExplicitGenerator
import world.gregs.yaml.write.GeneratorConfiguration
import world.gregs.yaml.write.NormalGenerator
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

    val writer = CharWriter()
    val defaultGenConfig = GeneratorConfiguration()
    private val explicitGen = ExplicitGenerator(defaultGenConfig, writer)
    private val normalGen = NormalGenerator(explicitGen, defaultGenConfig, writer)

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> load(path: String, config: CollectionConfiguration = defaultConfig): T {
        val file = File(path)
        val charArray = CharArray(file.length().toInt())
        val length = file.reader().use {
            it.read(charArray)
        }
        return parse(charArray, length, config) as T
    }

    fun parse(string: String, config: CollectionConfiguration = defaultConfig) = parse(string.toCharArray(), config = config)

    fun parse(charArray: CharArray, length: Int = charArray.size, config: CollectionConfiguration = defaultConfig): Any {
        explicit.config = config
        normal.config = config
        reader.anchors.clear()
        reader.set(charArray, length)
        reader.nextLine()
        return normal.value(indentOffset = 0, withinMap = null)
    }

    fun save(path: String, value: Any, config: GeneratorConfiguration = defaultGenConfig) {
        val array = write(value, config)
        File(path).writer().use {
            it.write(array)
        }
    }

    private fun write(value: Any, config: GeneratorConfiguration = defaultGenConfig): CharArray {
        explicitGen.config = config
        normalGen.config = config
        writer.clear()
        if (config.forceExplicit) {
            explicitGen.value(value, 0)
        } else {
            normalGen.value(value, 0)
        }
        return writer.toCharArray()
    }

    fun string(value: Any, config: GeneratorConfiguration = defaultGenConfig): String {
        val array = write(value, config)
        return array.concatToString()
    }
}