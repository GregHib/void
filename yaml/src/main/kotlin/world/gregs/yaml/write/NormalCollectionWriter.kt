package world.gregs.yaml.write

import world.gregs.yaml.CharWriter

/**
 * Writes lists and maps across multiple lines
 */
class NormalCollectionWriter(writer: CharWriter, config: YamlWriterConfiguration, val explicit: ExplicitCollectionWriter) : YamlWriter(writer, config) {

    override fun list(list: List<*>, indent: Int, parentMap: String?) {
        for (i in list.indices) {
            writer.append('-')
            writer.append(' ')
            when (val element = list[i]) {
                is List<*> -> explicit.list(element, indent + 1, parentMap = null)
                is Map<*, *> -> map(element, indent + 1, parentMap = null)
                else -> value(element, indent + 1, parentMap = null)
            }
            if (i < list.lastIndex) {
                writer.appendLine()
            }
        }
    }

    override fun map(map: Map<*, *>, indent: Int, parentMap: String?) {
        var index = 0
        for ((k, value) in map) {
            if (config.quoteKeys) {
                writer.append('"')
            }
            val key = k.toString()
            write(key)
            if (config.quoteKeys) {
                writer.append('"')
            }
            writer.append(':')
            index++
            when (value) {
                is Map<*, *> -> {
                    writer.appendLine()
                    writer.indent(indent + 1)
                    map(value, indent + 1, key)
                }
                is List<*> -> {
                    writer.appendLine()
                    writer.indent(indent + 1)
                    list(value, indent + 1, key)
                }
                else -> {
                    writer.append(' ')
                    value(value, indent, key)
                }
            }
            if (index < map.size) {
                writer.appendLine()
                writer.indent(indent)
            }
        }
    }
}