package world.gregs.yaml.write

import world.gregs.yaml.CharWriter

class ExplicitCollectionWriter(writer: CharWriter, config: YamlWriterConfiguration) : YamlWriter(writer, config) {

    override fun list(list: List<*>, indent: Int) {
        if (config.formatExplicit) {
            formattedList(list, indent)
            return
        }
        writer.append('[')
        writer.append(' ')
        for (i in list.indices) {
            value(list[i], indent)
            if (i != list.lastIndex) {
                writer.append(',')
                writer.append(' ')
            }
        }
        writer.append(' ')
        writer.append(']')
    }

    override fun map(map: Map<*, *>, indent: Int) {
        if (config.formatExplicit) {
            formattedMap(map, indent)
            return
        }
        writer.append('{')
        writer.append(' ')
        var i = 0
        for ((key, value) in map) {
            if (config.quoteKeys) {
                writer.append('"')
            }
            write(key.toString())
            if (config.quoteKeys) {
                writer.append('"')
            }
            writer.append(':')
            writer.append(' ')
            value(value, -1)
            if (i++ != map.size - 1) {
                writer.append(',')
                writer.append(' ')
            }
        }
        writer.append(' ')
        writer.append('}')
    }

    private fun formattedList(list: List<*>, indent: Int) {
        writer.append('[')
        writer.appendLine()
        for (i in list.indices) {
            writer.indent(indent + 1)
            value(list[i], indent + 1)
            if (i != list.lastIndex) {
                writer.append(',')
            }
            writer.appendLine()
        }
        writer.indent(indent)
        writer.append(']')
    }

    private fun formattedMap(map: Map<*, *>, indent: Int) {
        writer.append('{')
        writer.appendLine()
        var i = 0
        for ((key, value) in map) {
            writer.indent(indent + 1)
            if (config.quoteKeys) {
                writer.append('"')
            }
            write(key.toString())
            if (config.quoteKeys) {
                writer.append('"')
            }
            writer.append(':')
            writer.append(' ')
            value(value, indent + 1)
            if (i++ != map.size - 1) {
                writer.append(',')
            }
            writer.appendLine()
        }
        writer.indent(indent)
        writer.append('}')
    }
}