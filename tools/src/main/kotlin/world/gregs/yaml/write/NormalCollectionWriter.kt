package world.gregs.yaml.write

import world.gregs.yaml.CharWriter

/**
 * Writes lists and maps across multiple lines
 */
class NormalCollectionWriter(writer: CharWriter, config: YamlWriterConfiguration, val explicit: ExplicitCollectionWriter) : YamlWriter(writer, config) {

    override fun list(list: List<*>, indent: Int, parentMap: String?) {
        if (config.explicit(list, indent, parentMap)) {
            explicit.list(list, indent, parentMap)
            return
        }
        for (i in list.indices) {
            writer.append('-')
            writer.append(' ')
            when (val element = config.write(list[i], indent, parentMap) ?: continue) {
                is List<*> -> explicit.list(element, indent + 1, parentMap = null)
                is Array<*> -> explicit.list(element.toList(), indent + 1, parentMap = null)
                is BooleanArray -> explicit.list(element.toList(), indent + 1, parentMap = null)
                is ByteArray -> explicit.list(element.toList(), indent + 1, parentMap = null)
                is CharArray -> explicit.list(element.toList(), indent + 1, parentMap = null)
                is ShortArray -> explicit.list(element.toList(), indent + 1, parentMap = null)
                is FloatArray -> explicit.list(element.toList(), indent + 1, parentMap = null)
                is IntArray -> explicit.list(element.toList(), indent + 1, parentMap = null)
                is DoubleArray -> explicit.list(element.toList(), indent + 1, parentMap = null)
                is LongArray -> explicit.list(element.toList(), indent + 1, parentMap = null)
                is Map<*, *> -> map(element, indent + 1, parentMap = null)
                else -> value(element, indent + 1, parentMap = null)
            }
            if (i < list.lastIndex) {
                writer.appendLine()
                writer.indent(indent)
            }
        }
    }

    override fun map(map: Map<*, *>, indent: Int, parentMap: String?) {
        var index = 0
        for ((k, v) in map) {
            val key = writeKey(k)
            index++
            when (val value = config.write(v, indent, parentMap) ?: continue) {
                is Map<*, *> -> {
                    writer.appendLine()
                    writer.indent(indent + 1)
                    map(value, indent + 1, key)
                }
                is List<*> -> setList(indent, value, key)
                is Array<*> -> setList(indent, value.toList(), key)
                is BooleanArray -> setList(indent, value.toList(), key)
                is ByteArray -> setList(indent, value.toList(), key)
                is CharArray -> setList(indent, value.toList(), key)
                is ShortArray -> setList(indent, value.toList(), key)
                is FloatArray -> setList(indent, value.toList(), key)
                is IntArray -> setList(indent, value.toList(), key)
                is DoubleArray -> setList(indent, value.toList(), key)
                is LongArray -> setList(indent, value.toList(), key)
                else -> {
                    if (key != "&") {
                        writer.append(' ')
                    }
                    value(value, indent, key)
                }
            }
            if (index < map.size) {
                writer.appendLine()
                writer.indent(indent)
            }
        }
    }

    private fun setList(indent: Int, value: List<Any?>, key: String) {
        if (config.explicit(value, indent, key)) {
            writer.append(' ')
        } else {
            writer.appendLine()
            writer.indent(indent + 1)
        }
        list(value, indent + 1, key)
    }
}
