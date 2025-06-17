package world.gregs.yaml.write

import world.gregs.yaml.CharWriter

/**
 * Writes values of any type to [CharWriter]
 */
abstract class YamlWriter(val writer: CharWriter, var config: YamlWriterConfiguration) {

    fun value(value: Any?, indent: Int, parentMap: String?) {
        when (val v = value ?: return) {
            is String -> string(v, parentMap)
            is List<*> -> list(v, indent, parentMap)
            is Map<*, *> -> map(v, indent, parentMap)
            is Array<*> -> list(v.toList(), indent, parentMap)
            is BooleanArray -> list(v.toList(), indent, parentMap)
            is ByteArray -> list(v.toList(), indent, parentMap)
            is CharArray -> list(v.toList(), indent, parentMap)
            is ShortArray -> list(v.toList(), indent, parentMap)
            is FloatArray -> list(v.toList(), indent, parentMap)
            is IntArray -> list(v.toList(), indent, parentMap)
            is DoubleArray -> list(v.toList(), indent, parentMap)
            is LongArray -> list(v.toList(), indent, parentMap)
            else -> write(config.toString(v, indent, parentMap))
        }
    }

    abstract fun list(list: List<*>, indent: Int, parentMap: String?)

    abstract fun map(map: Map<*, *>, indent: Int, parentMap: String?)

    fun string(value: String, parentMap: String?) {
        val quote = quoteString(value, parentMap)
        if (quote) {
            writer.append('"')
        }
        write(if (quote) value.replace("\"", "\\\"") else value)
        if (quote) {
            writer.append('"')
        }
    }

    private fun quoteString(value: String, parentMap: String?): Boolean = !anchor(value, parentMap) && (config.forceQuoteStrings || config.quoteStrings && value.contains(' '))

    private fun anchor(value: String, parentMap: String?) = parentMap == "&" || parentMap == "<<" || value.startsWith('&') || (value.startsWith('*') && !value.contains(' ') && value.count { it == '*' } == 1)

    fun write(value: String) {
        for (char in value) {
            writer.append(char)
        }
    }

    open fun writeKey(k: Any?): String {
        val key = k.toString()
        val quote = quoteKey(key)
        if (quote) {
            writer.append('"')
        }
        write(key)
        if (quote) {
            writer.append('"')
        }
        if (key != "&") {
            writer.append(':')
        }
        return key
    }

    private fun quoteKey(key: String) = config.forceQuoteKeys || config.quoteKeys && key.contains(' ')
}
