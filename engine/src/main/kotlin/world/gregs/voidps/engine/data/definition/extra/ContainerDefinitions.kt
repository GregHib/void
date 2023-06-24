package world.gregs.voidps.engine.data.definition.extra

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import world.gregs.voidps.cache.config.data.ContainerDefinition
import world.gregs.voidps.cache.config.decoder.ContainerDecoder
import world.gregs.voidps.engine.data.definition.DefinitionsDecoder
import world.gregs.voidps.engine.data.yaml.YamlParser
import world.gregs.voidps.engine.data.yaml.config.DefinitionConfig
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.timedLoad

class ContainerDefinitions(
    decoder: ContainerDecoder
) : DefinitionsDecoder<ContainerDefinition> {

    override lateinit var definitions: Array<ContainerDefinition>
    override lateinit var ids: Map<String, Int>

    init {
        val start = System.currentTimeMillis()
        definitions = decoder.indices.map { decoder.get(it) }.toTypedArray()
        timedLoad("container definition", definitions.size, start)
    }

    override fun empty() = ContainerDefinition.EMPTY

    @Suppress("UNCHECKED_CAST")
    fun load(parser: YamlParser = get(), path: String = getProperty("containerDefinitionsPath"), itemDefs: ItemDefinitions = get()): ContainerDefinitions {
        timedLoad("container extra") {
            val ids = Object2IntOpenHashMap<String>()
            val config = object : DefinitionConfig<ContainerDefinition>(ids, definitions) {
                override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) { 
                    if (key == "defaults" && value is List<*>) {
                        val id = map["id"] as Int
                        value as List<Map<String, Int>>
                        val def = definitions[id]
                        def.ids = IntArray(def.length) { itemDefs.get(value[it].keys.first()).id }
                        def.amounts = IntArray(def.length) { value[it].values.first() }
                    }
                    super.set(map, key, value, indent, parentMap)
                }
            }
            parser.load<Any>(path, config)
            this.ids = ids
            ids.size
        }
        return this
    }
}

fun ContainerDefinition.items(): List<String> {
    val defs: ItemDefinitions = get()
    return ids?.map { defs.get(it).stringId } ?: emptyList()
}