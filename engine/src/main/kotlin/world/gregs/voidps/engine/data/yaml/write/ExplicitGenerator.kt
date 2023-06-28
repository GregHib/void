package world.gregs.voidps.engine.data.yaml.write

import world.gregs.voidps.engine.data.yaml.CharWriter

class ExplicitGenerator(config: GeneratorConfiguration, writer: CharWriter) : Generator(config, writer) {

    override fun list(list: List<*>, indent: Int) {
        writer.append('[')
        writer.append(' ')
        for (i in list.indices) {
            value(list[i], -1)
            if (i != list.lastIndex) {
                writer.append(',')
                writer.append(' ')
            }
        }
        writer.append(' ')
        writer.append(']')
    }

    override fun map(map: Map<*, *>, indent: Int) {
        writer.append('{')
        writer.append(' ')
        var i = 0
        for ((key, value) in map) {
            if (config.quoteKeys) {
                writer.append('"')
            }
            string(key.toString())
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
}