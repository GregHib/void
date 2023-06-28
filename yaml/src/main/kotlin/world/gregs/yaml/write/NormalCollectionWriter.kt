package world.gregs.yaml.write

import world.gregs.yaml.CharWriter

/**
 * Writes lists and maps across multiple lines
 */
class NormalCollectionWriter(writer: CharWriter, config: YamlWriterConfiguration, val explicit: ExplicitCollectionWriter) : YamlWriter(writer, config) {

    override fun list(list: List<*>, indent: Int, parentMap: String?) {
        for (element in list) {
            writer.indent(indent)
            writer.append('-')
            writer.append(' ')
            if (element is List<*>) {
                explicit.list(element, indent + 1, parentMap = null)
            } else {
                value(element, indent, parentMap = null)
            }
            writer.appendLine()
        }
    }

    override fun map(map: Map<*, *>, indent: Int, parentMap: String?) {
        for ((k, value) in map) {
            writer.indent(indent)
            if (config.quoteKeys) {
                writer.append('"')
            }
            val key = k.toString()
            write(key)
            if (config.quoteKeys) {
                writer.append('"')
            }
            writer.append(':')
            when (value) {
                is Map<*, *> -> {
                    writer.appendLine()
                    map(value, indent + 1, key)
                }
                is List<*> -> {
                    writer.appendLine()
                    list(value, indent + 1, key)
                }
                else -> {
                    writer.append(' ')
                    value(value, indent, key)
                    writer.appendLine()
                }
            }
        }
    }
}