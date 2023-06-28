package world.gregs.yaml.write

import world.gregs.yaml.CharWriter

class NormalCollectionWriter(writer: CharWriter, config: YamlWriterConfiguration, val explicit: ExplicitCollectionWriter) : YamlWriter(writer, config) {

    override fun list(list: List<*>, indent: Int) {
        for (element in list) {
            writer.indent(indent)
            writer.append('-')
            writer.append(' ')
            if (element is List<*>) {
                explicit.list(element, -1)
            } else {
                value(element, indent)
            }
            writer.appendLine()
        }
    }

    override fun map(map: Map<*, *>, indent: Int) {
        for ((key, value) in map) {
            writer.indent(indent)
            if (config.quoteKeys) {
                writer.append('"')
            }
            string(key.toString())
            if (config.quoteKeys) {
                writer.append('"')
            }
            writer.append(':')
            when (value) {
                is Map<*, *> -> {
                    writer.appendLine()
                    map(value, indent + 1)
                }
                is List<*> -> {
                    writer.appendLine()
                    list(value, indent + 1)
                }
                else -> {
                    writer.append(' ')
                    value(value, indent)
                    writer.appendLine()
                }
            }
        }
    }
}