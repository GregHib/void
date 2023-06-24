package world.gregs.voidps.engine.data.definition.extra

import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.client.ui.chat.toIntRange
import world.gregs.voidps.engine.data.definition.config.GearDefinition
import world.gregs.voidps.engine.data.yaml.YamlParser
import world.gregs.voidps.engine.data.yaml.config.FastUtilConfiguration
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.network.visual.update.player.EquipSlot

class GearDefinitions {

    private lateinit var definitions: Map<String, MutableList<GearDefinition>>

    fun get(style: String): List<GearDefinition> = definitions[style] ?: emptyList()

    @Suppress("UNCHECKED_CAST")
    fun load(parser: YamlParser = get(), path: String = getProperty("gearDefinitionsPath")): GearDefinitions {
        timedLoad("gear definition") {
            var count = 0
            val config = object : FastUtilConfiguration() {
                override fun add(list: MutableList<Any>, value: Any, parentMap: String?) {
                    if (parentMap == "inventory") {
                        value as Map<String, Any>
                        val id = value["id"]
                        if (id is List<*>) {
                            val amount = value["amount"] as? Int ?: 1
                            val subList = createList()
                            for (i in id as List<String>) {
                                subList.add(Item(i, amount, ItemDefinition.EMPTY))
                            }
                            super.add(list, subList, parentMap)
                        } else {
                            val subList = createList()
                            subList.add(Item(value["id"] as String, value["amount"] as? Int ?: 1, ItemDefinition.EMPTY))
                            super.add(list, subList, parentMap)
                        }
                    } else if (parentMap == "equipment") {
                        value as Map<String, List<Item>>
                        super.add(list, value.mapKeys { EquipSlot.valueOf(it.key.toSentenceCase()) }, parentMap)
                    } else if (value is Map<*, *> && value.containsKey("id")) {
                        val item = Item(value["id"] as String, value["amount"] as? Int ?: 1, ItemDefinition.EMPTY)
                        super.add(list, item, parentMap)
                    } else if(parentMap != "id") {
                        count++
                        super.add(list, GearDefinition(parentMap!!, value as Map<String, Any>), parentMap)
                    } else {
                        super.add(list, value, parentMap)
                    }
                }

                override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) { 
                    super.set(map, key, when (key) {
                        "levels" -> (value as String).toIntRange()
                        else -> value
                    }, indent, parentMap)
                }
            }
            this.definitions = parser.load(path, config)
            count
        }
        return this
    }
}