package world.gregs.voidps.engine.data.definition.extra

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder
import world.gregs.voidps.engine.client.ui.chat.toIntRange
import world.gregs.voidps.engine.data.DefinitionConfig
import world.gregs.voidps.engine.data.definition.DefinitionsDecoder
import world.gregs.voidps.engine.data.definition.data.Pickable
import world.gregs.voidps.engine.data.definition.data.Rock
import world.gregs.voidps.engine.data.definition.data.Tree
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml

class ObjectDefinitions(
    decoder: ObjectDecoder
) : DefinitionsDecoder<ObjectDefinition> {

    override lateinit var definitions: Array<ObjectDefinition>
    override lateinit var ids: Map<String, Int>

    init {
        val start = System.currentTimeMillis()
        definitions = decoder.indices.map { decoder.get(it) }.toTypedArray()
        timedLoad("object definition", definitions.size, start)
    }

    override fun get(id: Int): ObjectDefinition {
        return definitions[id]
    }

    override fun empty() = ObjectDefinition.EMPTY

    @Suppress("UNCHECKED_CAST")
    fun load(yaml: Yaml = get(), path: String = getProperty("objectDefinitionsPath")): ObjectDefinitions {
        timedLoad("object extra") {
            val ids = Object2IntOpenHashMap<String>()
            this.ids = ids
            val config = object : DefinitionConfig<ObjectDefinition>(ids, definitions) {

                override fun add(list: MutableList<Any>, value: Any, parentMap: String?) {
                    if (parentMap == "ores") {
                        super.add(list, Item(value as String, def = ItemDefinition.EMPTY), parentMap)
                    } else {
                        super.add(list, value, parentMap)
                    }
                }

                override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                    if (key == "<<") {
                        map.putAll(value as Map<String, Any>)
                    } else if (indent == 1) {
                        super.set(map, key,
                            when (key) {
                                "pickable" -> Pickable(value as Map<String, Any>)
                                "woodcutting" -> Tree(value as Map<String, Any>)
                                "mining" -> Rock(value as Map<String, Any>)
                                else -> value
                            }, indent, parentMap)
                    } else {
                        super.set(map, key, when (key) {
                            "chance", "hatchet_low_dif", "hatchet_high_dif", "respawn" -> (value as String).toIntRange()
                            "item", "log" -> Item(value as String, def = ItemDefinition.EMPTY)
                            else -> value
                        }, indent, parentMap)
                    }
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