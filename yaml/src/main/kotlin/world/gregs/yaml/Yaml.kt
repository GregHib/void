package world.gregs.yaml

import world.gregs.yaml.read.ExplicitCollectionReader
import world.gregs.yaml.read.NormalCollectionReader
import world.gregs.yaml.read.YamlReaderConfiguration
import world.gregs.yaml.write.ExplicitCollectionWriter
import world.gregs.yaml.write.NormalCollectionWriter
import world.gregs.yaml.write.YamlWriterConfiguration
import java.io.File

/**
 * High performance parser for reading and writing simplified YAML (and json)
 */
class Yaml(
    private val defaultReader: YamlReaderConfiguration = YamlReaderConfiguration(),
    private val defaultWriter: YamlWriterConfiguration = YamlWriterConfiguration(),
) {

    private val writer = CharWriter()
    private val reader: CharReader = CharReader(defaultReader.createMap())

    private val explicitReader: ExplicitCollectionReader = ExplicitCollectionReader(reader, defaultReader)
    private val normalReader: NormalCollectionReader = NormalCollectionReader(reader, defaultReader, explicitReader)
    private val explicitWriter = ExplicitCollectionWriter(writer, defaultWriter)
    private val normalWriter = NormalCollectionWriter(writer, defaultWriter, explicitWriter)

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> load(path: String, config: YamlReaderConfiguration = defaultReader): T {
        val file = File(path)
        val charArray = CharArray(file.length().toInt())
        val length = file.reader().use {
            it.read(charArray)
        }
        return read(charArray, length, config) as T
    }

    fun save(path: String, value: Any, config: YamlWriterConfiguration = defaultWriter) {
        save(File(path), value, config)
    }

    fun save(file: File, value: Any, config: YamlWriterConfiguration = defaultWriter) {
        val array = write(value, config)
        file.writer().use {
            it.write(array)
        }
    }

    fun read(string: String, config: YamlReaderConfiguration = defaultReader) = read(string.toCharArray(), config = config)

    fun read(charArray: CharArray, length: Int = charArray.size, config: YamlReaderConfiguration = defaultReader): Any {
        explicitReader.config = config
        normalReader.config = config
//        reader.anchors.clear()
        reader.set(charArray, length)
        reader.nextLine()
        if (reader.outBounds) {
            return Unit
        }
        return normalReader.value(indentOffset = 0, withinMap = null)
    }

    fun writeToString(value: Any, config: YamlWriterConfiguration = defaultWriter): String = write(value, config).concatToString()

    private fun write(value: Any, config: YamlWriterConfiguration = defaultWriter): CharArray {
        explicitWriter.config = config
        normalWriter.config = config
        writer.clear()
        val v = config.write(value, 0, null)
        if (config.forceExplicit) {
            explicitWriter.value(v, 0, null)
        } else {
            normalWriter.value(v, 0, null)
        }
        return writer.toCharArray()
    }
}
