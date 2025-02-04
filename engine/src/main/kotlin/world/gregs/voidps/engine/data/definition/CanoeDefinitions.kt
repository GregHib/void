package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.cache.definition.data.CanoeDefinition
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml
import world.gregs.yaml.read.YamlReaderConfiguration

class CanoeDefinitions {

    lateinit var definitions: Map<String, CanoeDefinition>

    fun get(id: String): CanoeDefinition = getOrNull(id) ?: CanoeDefinition.EMPTY

    fun getOrNull(id: String): CanoeDefinition? = definitions[id]

    fun empty() = CanoeDefinition.EMPTY

    @Suppress("UNCHECKED_CAST")
    fun load(yaml: Yaml = get(), path: String = Settings["map.canoes"]): CanoeDefinitions {
        timedLoad("canoe station") {
            var count = 0
            val canoes = Object2ObjectOpenHashMap<String, CanoeDefinition>()
            val config = object : YamlReaderConfiguration(2, 2) {
                override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                    if (indent == 0) {
                        count++
                        canoes[key] = CanoeDefinition.fromMap(key, value as MutableMap<String, Any>)
                    } else {
                        super.set(map, key, value, indent, parentMap)
                    }
                }
            }
            yaml.load<Any>(path, config)
            this.definitions = canoes
            count
        }
        return this
    }

}