package world.gregs.yaml.write

import world.gregs.yaml.CharWriter

/**
 * Writes collections in square or curley brackets on one or across multiple lines
 */
class ExplicitCollectionWriter(writer: CharWriter, config: YamlWriterConfiguration) : YamlWriter(writer, config) {

    override fun list(list: List<*>, indent: Int, parentMap: String?) {
        if (list.size > config.formatExplicitListSizeLimit) {
            formattedList(list, indent, parentMap)
            return
        }
        writer.append('[')
        if (list.isNotEmpty()) {
            writer.append(' ')
            for (i in list.indices) {
                val element = config.write(list[i], indent, parentMap)
                value(element, indent, parentMap)
                if (i != list.lastIndex) {
                    writer.append(',')
                    writer.append(' ')
                }
            }
            writer.append(' ')
        }
        writer.append(']')
    }

    override fun map(map: Map<*, *>, indent: Int, parentMap: String?) {
        if (config.formatExplicitMap) {
            formattedMap(map, indent)
            return
        }
        writer.append('{')
        if (map.isNotEmpty()) {
            writer.append(' ')
            var i = 0
            for ((k, v) in map) {
                val key = writeKey(k)
                val value = config.write(v, indent, parentMap)
                value(value, indent, key)
                if (i++ != map.size - 1) {
                    writer.append(',')
                    writer.append(' ')
                }
            }
            writer.append(' ')
        }
        writer.append('}')
    }

    private fun formattedList(list: List<*>, indent: Int, parentMap: String?) {
        writer.append('[')
        if (list.isNotEmpty()) {
            writer.appendLine()
            for (i in list.indices) {
                writer.indent(indent + 1)
                val element = config.write(list[i], indent + 1, parentMap)
                value(element, indent + 1, parentMap)
                if (i != list.lastIndex) {
                    writer.append(',')
                }
                writer.appendLine()
            }
            writer.indent(indent)
        }
        writer.append(']')
    }

    private fun formattedMap(map: Map<*, *>, indent: Int) {
        writer.append('{')
        if (map.isNotEmpty()) {
            writer.appendLine()
            var i = 0
            for ((k, v) in map) {
                writer.indent(indent + 1)
                val key = writeKey(k)
                val value = config.write(v, indent + 1, key)
                value(value, indent + 1, key)
                if (i++ != map.size - 1) {
                    writer.append(',')
                }
                writer.appendLine()
            }
            writer.indent(indent)
        }
        writer.append('}')
    }

    override fun writeKey(k: Any?): String {
        val key = super.writeKey(k)
        if (key != "&") {
            writer.append(' ')
        }
        return key
    }
}
