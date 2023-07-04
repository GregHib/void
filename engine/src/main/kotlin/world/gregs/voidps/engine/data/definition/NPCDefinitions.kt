package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.data.definition.data.Spot
import world.gregs.voidps.engine.data.yaml.DefinitionConfig
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml

class NPCDefinitions(
    override var definitions: Array<NPCDefinition>
) : DefinitionsDecoder<NPCDefinition> {

    override lateinit var ids: Map<String, Int>

    override fun empty() = NPCDefinition.EMPTY

    @Suppress("UNCHECKED_CAST")
    fun load(yaml: Yaml = get(), path: String = getProperty("npcDefinitionsPath"), itemDefinitions: ItemDefinitions = get()): NPCDefinitions {
        timedLoad("npc extra") {
            val ids = Object2IntOpenHashMap<String>()
            this.ids = ids
            val config = object : DefinitionConfig<NPCDefinition>(ids, definitions) {
                override fun add(list: MutableList<Any>, value: Any, parentMap: String?) {
                    super.add(list, Item(value as String, amount = 1, def = itemDefinitions.get(value)), parentMap)
                }

                override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                    if (key == "<<") {
                        map.putAll(value as Map<String, Any>)
                        return
                    }
                    super.set(map, key,
                        if (indent == 1 && key == "fishing") {
                            (value as Map<String, Map<String, Any>>).mapValues { Spot(it.value) }
                        } else {
                            value
                        }, indent, parentMap)
                }
            }
            yaml.load<Any>(path, config)
            for (def in definitions) {
                def.transforms = def.transformIds?.map { if (it == -1) null else get(it).stringId }?.toTypedArray()
            }
            ids.size
        }
        return this
    }

}