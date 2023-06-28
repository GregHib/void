package world.gregs.yaml.write

import world.gregs.yaml.CharWriter

abstract class YamlWriter(val writer: CharWriter, var config: YamlWriterConfiguration) {

    fun value(value: Any?, indent: Int) {
        when (value) {
            is List<*> -> list(value, indent)
            is Map<*, *> -> map(value, indent)
            is String -> string(value)
            else -> append(value)
        }
    }

    abstract fun list(list: List<*>, indent: Int)

    abstract fun map(map: Map<*, *>, indent: Int)

    fun string(value: String) {
        if (config.quoteStrings) {
            writer.append('"')
        }
        write(value)
        if (config.quoteStrings) {
            writer.append('"')
        }
    }

    fun append(value: Any?) {
        write(value.toString())
    }

    fun write(value: String) {
        for (char in value) {
            writer.append(char)
        }
    }
}