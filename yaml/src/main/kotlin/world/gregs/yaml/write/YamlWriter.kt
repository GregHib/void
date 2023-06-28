package world.gregs.yaml.write

import world.gregs.yaml.CharWriter

/**
 * Writes values of any type to [CharWriter]
 */
abstract class YamlWriter(val writer: CharWriter, var config: YamlWriterConfiguration) {

    fun value(value: Any?, indent: Int, parentMap: String?) {
        when (val v = config.write(value, indent, parentMap) ?: return) {
            is String -> string(v)
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

    fun string(value: String) {
        if (config.quoteStrings) {
            writer.append('"')
        }
        write(value)
        if (config.quoteStrings) {
            writer.append('"')
        }
    }

    fun write(value: String) {
        for (char in value) {
            writer.append(char)
        }
    }
}