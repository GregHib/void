package world.gregs.yaml.write

import world.gregs.yaml.CharWriter

/**
 * Writes collections in square or curley brackets on one or across multiple lines
 */
class ExplicitCollectionWriter(writer: CharWriter, config: YamlWriterConfiguration) : YamlWriter(writer, config) {

    override fun list(list: List<*>, indent: Int, parentMap: String?) {
        if (config.formatExplicit) {
            formattedList(list, indent, parentMap)
            return
        }
        writer.append('[')
        writer.append(' ')
        for (i in list.indices) {
            value(list[i], indent, parentMap)
            if (i != list.lastIndex) {
                writer.append(',')
                writer.append(' ')
            }
        }
        writer.append(' ')
        writer.append(']')
    }

    override fun map(map: Map<*, *>, indent: Int, parentMap: String?) {
        if (config.formatExplicit) {
            formattedMap(map, indent, parentMap)
            return
        }
        writer.append('{')
        writer.append(' ')
        var i = 0
        for ((k, value) in map) {
            val key = writeKey(k)
            value(value, indent, key)
            if (i++ != map.size - 1) {
                writer.append(',')
                writer.append(' ')
            }
        }
        writer.append(' ')
        writer.append('}')
    }

    private fun formattedList(list: List<*>, indent: Int, parentMap: String?) {
        writer.append('[')
        writer.appendLine()
        for (i in list.indices) {
            writer.indent(indent + 1)
            value(list[i], indent + 1, parentMap)
            if (i != list.lastIndex) {
                writer.append(',')
            }
            writer.appendLine()
        }
        writer.indent(indent)
        writer.append(']')
    }

    private fun formattedMap(map: Map<*, *>, indent: Int, parentMap: String?) {
        writer.append('{')
        writer.appendLine()
        var i = 0
        for ((k, value) in map) {
            writer.indent(indent + 1)
            val key = writeKey(k)
            value(value, indent + 1, key)
            if (i++ != map.size - 1) {
                writer.append(',')
            }
            writer.appendLine()
        }
        writer.indent(indent)
        writer.append('}')
    }

    private fun writeKey(k: Any?): String {
        if (config.quoteKeys) {
            writer.append('"')
        }
        val key = k.toString()
        write(key)
        if (config.quoteKeys) {
            writer.append('"')
        }
        writer.append(':')
        writer.append(' ')
        return key
    }
}