package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.client.ui.chat.toIntRange
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.GearDefinition
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.yaml.Yaml
import world.gregs.yaml.read.YamlReaderConfiguration

class GearDefinitions {

    private lateinit var definitions: Map<String, MutableList<GearDefinition>>

    fun get(style: String): List<GearDefinition> = definitions[style] ?: emptyList()

    @Suppress("UNCHECKED_CAST")
    fun load(yaml: Yaml = get(), path: String = Settings["definitions.gearSets"]): GearDefinitions {
        timedLoad("gear definition") {
            var count = 0
            val config = object : YamlReaderConfiguration() {
                override fun add(list: MutableList<Any>, value: Any, parentMap: String?) {
                    if (parentMap == "inventory") {
                        value as Map<String, Any>
                        val id = value["id"]
                        if (id is List<*>) {
                            val amount = value["amount"] as? Int ?: 1
                            val subList = createList()
                            for (i in id as List<String>) {
                                subList.add(Item(i, amount))
                            }
                            super.add(list, subList, parentMap)
                        } else {
                            val subList = createList()
                            subList.add(Item(id as String, value["amount"] as? Int ?: 1))
                            super.add(list, subList, parentMap)
                        }
                    } else if (parentMap == "equipment") {
                        value as Map<String, List<Item>>
                        super.add(list, Object2ObjectOpenHashMap(value.mapKeys { EquipSlot.valueOf(it.key.toSentenceCase()) }), parentMap)
                    } else if (value is Map<*, *> && value.containsKey("id")) {
                        val id = value["id"] as String
                        val item = Item(id, value["amount"] as? Int ?: 1)
                        super.add(list, item, parentMap)
                    } else if (parentMap != "id" && value is Map<*, *>) {
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
            this.definitions = yaml.load(path, config)
            count
        }
        return this
    }
}