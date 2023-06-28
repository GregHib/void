package world.gregs.voidps.engine.data.definition.extra

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.cache.definition.decoder.NPCDecoder
import world.gregs.voidps.engine.data.DefinitionConfig
import world.gregs.voidps.engine.data.definition.DefinitionsDecoder
import world.gregs.voidps.engine.data.definition.data.Spot
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.YamlParser

class NPCDefinitions(
    decoder: NPCDecoder
) : DefinitionsDecoder<NPCDefinition> {

    override lateinit var definitions: Array<NPCDefinition>
    override lateinit var ids: Map<String, Int>

    init {
        val start = System.currentTimeMillis()
        definitions = decoder.indices.map { decoder.get(it) }.toTypedArray()
        timedLoad("npc definition", definitions.size, start)
    }

    override fun empty() = NPCDefinition.EMPTY

    @Suppress("UNCHECKED_CAST")
    fun load(parser: YamlParser = get(), path: String = getProperty("npcDefinitionsPath"), itemDefinitions: ItemDefinitions = get()): NPCDefinitions {
        timedLoad("npc extra") {
            val ids = Object2IntOpenHashMap<String>()
            this.ids = ids
            val config = object : DefinitionConfig<NPCDefinition>(ids, definitions) {
                override fun add(list: MutableList<Any>, value: Any, parentMap: String?) {
                    if (parentMap == "items" || parentMap == "bait") {
                        super.add(list, Item(value as String, def = ItemDefinition.EMPTY), parentMap)
                    } else {
                        super.add(list, value, parentMap)
                    }
                }

                override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                    super.set(map, key,
                        if (indent == 0 && key == "fishing") {
                            (value as Map<String, Map<String, Any>>).mapValues { Spot(it.value) }
                        } else {
                            value
                        }, indent, parentMap)
                }
            }
            parser.load<Any>(path, config)
            for (def in definitions) {
                def.transforms = def.transformIds?.map { if (it == -1) null else get(it).stringId }?.toTypedArray()
            }
            ids.size
        }
        return this
    }

}