package world.gregs.voidps.engine.data

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Extra
import world.gregs.voidps.engine.data.definition.DefinitionsDecoder
import world.gregs.yaml.YamlParser
import world.gregs.yaml.config.FastUtilConfiguration

class MapDefinitionConfig<T : Extra>(
    val producer: (id: Int, key: String, extras: Map<String, Any>?) -> T
) : FastUtilConfiguration() {
    val ids = Object2IntOpenHashMap<String>()
    val definitions = Int2ObjectOpenHashMap<T>()

    @Suppress("UNCHECKED_CAST")
    override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
        if (value is Int && indent == 0) {
            ids[key] = value
            definitions[value] = producer(value, key, null)
        } else if (indent == 0) {
            value as MutableMap<String, Any>
            val id = value.remove("id") as Int
            ids[key] = id
            definitions[id] = producer(id, key, value)
        } else {
            super.set(map, key, value, indent, parentMap)
        }
    }
}

inline fun <reified T> DefinitionsDecoder<T>.decode(parser: YamlParser, path: String, noinline producer: (id: Int, key: String, extras: Map<String, Any>?) -> T): Int where T : Definition, T : Extra {
    val config = MapDefinitionConfig(producer)
    parser.load<Any>(path, config)
    ids = config.ids
    this.definitions = Array(config.definitions.keys.max()) { config.definitions.get(it) ?: producer(it, it.toString(), null) }
    return ids.size
}