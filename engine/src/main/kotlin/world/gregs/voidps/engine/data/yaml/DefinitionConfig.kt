package world.gregs.voidps.engine.data.yaml

import world.gregs.voidps.cache.definition.Extra
import world.gregs.yaml.read.YamlReader

open class DefinitionConfig<T : Extra>(
    val ids: MutableMap<String, Int>,
    val definitions: Array<T>
) : DefinitionIdsConfig() {
    override fun set(map: MutableMap<String, Any>, key: String, id: Int, extras: Map<String, Any>?) {
        if (id !in definitions.indices) {
            return
        }
        ids[key] = id
        val def = definitions[id]
        def.stringId = key
        val existing = def.extras
        if (existing != null && extras != null) {
            existing as MutableMap<String, Any>
            existing.putAll(extras)
        } else if (extras != null) {
            def.extras = extras
        }
    }

    fun YamlReader.readIntRange(): IntRange {
        val start = reader.index
        val number = number(start)
        return if (reader.char == '-') {
            val int = (number ?: reader.number(false, start, reader.index)) as Int
            reader.skip()
            val second = number(reader.index)
            if (second != null) {
                int until second as Int
            } else {
                int until int
            }
        } else if (reader.char == '.') {
            val int = (number ?: reader.number(false, start, reader.index - 1)) as Int
            reader.skip()
            val second = number(reader.index)
            if (second != null) {
                int..second as Int
            } else {
                int..int
            }
        } else if (number != null) {
            number as Int..number
        } else {
            throw IllegalArgumentException("Unexpected value ${reader.exception}")
        }
    }
}