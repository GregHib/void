package world.gregs.voidps.engine.data.yaml.write

import world.gregs.voidps.engine.data.yaml.CharWriter

abstract class Generator(var config: GeneratorConfiguration, val writer: CharWriter) {

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

    fun append(value: Any?) {
        val string = value.toString()
        for (char in string) {
            writer.append(char)
        }
    }

    fun string(value: String) {
        if (config.quoteStrings) {
            writer.append('"')
        }
        for (char in value) {
            writer.append(char)
        }
        if (config.quoteStrings) {
            writer.append('"')
        }
    }
}