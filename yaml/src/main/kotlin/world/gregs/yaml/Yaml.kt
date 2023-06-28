package world.gregs.yaml

import world.gregs.yaml.read.ExplicitCollectionReader
import world.gregs.yaml.read.NormalCollectionReader
import world.gregs.yaml.read.YamlReaderConfiguration
import world.gregs.yaml.write.ExplicitCollectionWriter
import world.gregs.yaml.write.NormalCollectionWriter
import world.gregs.yaml.write.YamlWriterConfiguration
import java.io.File

/**
 * High performance parser for simplified YAML
 */
class Yaml(
    private val defaultConfig: YamlReaderConfiguration = YamlReaderConfiguration(),
    val reader: CharReader = CharReader(defaultConfig.createMap()),
    private val explicit: ExplicitCollectionReader = ExplicitCollectionReader(reader, defaultConfig),
    private val normal: NormalCollectionReader = NormalCollectionReader(reader, defaultConfig, explicit)
) {

    val writer = CharWriter()
    val defaultGenConfig = YamlWriterConfiguration()
    private val explicitGen = ExplicitCollectionWriter(defaultGenConfig, writer)
    private val normalGen = NormalCollectionWriter(explicitGen, defaultGenConfig, writer)

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> load(path: String, config: YamlReaderConfiguration = defaultConfig): T {
        val file = File(path)
        val charArray = CharArray(file.length().toInt())
        val length = file.reader().use {
            it.read(charArray)
        }
        return parse(charArray, length, config) as T
    }

    fun parse(string: String, config: YamlReaderConfiguration = defaultConfig) = parse(string.toCharArray(), config = config)

    fun parse(charArray: CharArray, length: Int = charArray.size, config: YamlReaderConfiguration = defaultConfig): Any {
        explicit.config = config
        normal.config = config
        reader.anchors.clear()
        reader.set(charArray, length)
        reader.nextLine()
        return normal.value(indentOffset = 0, withinMap = null)
    }

    fun save(path: String, value: Any, config: YamlWriterConfiguration = defaultGenConfig) {
        val array = write(value, config)
        File(path).writer().use {
            it.write(array)
        }
    }

    private fun write(value: Any, config: YamlWriterConfiguration = defaultGenConfig): CharArray {
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

    fun string(value: Any, config: YamlWriterConfiguration = defaultGenConfig): String {
        val array = write(value, config)
        return array.concatToString()
    }
}