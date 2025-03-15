package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.pearx.kasechange.toSentenceCase
import world.gregs.config.Config
import world.gregs.config.ConfigReader
import world.gregs.voidps.engine.client.ui.chat.toIntRange
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.GearDefinition
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class GearDefinitions {

    private lateinit var definitions: Map<String, List<GearDefinition>>

    fun get(style: String): List<GearDefinition> = definitions[style] ?: emptyList()

    fun load(path: String = Settings["definitions.gearSets"]): GearDefinitions {
        timedLoad("gear definition") {
            val definitions = Object2ObjectOpenHashMap<String, List<GearDefinition>>(100, Hash.VERY_FAST_LOAD_FACTOR)
            Config.fileReader(path) {
                while (nextPair()) {
                    val style = key()
                    val gear = ObjectArrayList<GearDefinition>(4)
                    while (nextElement()) {
                        var levels = 0..0
                        val inventory = ObjectArrayList<List<Item>>(2)
                        val equipment = Object2ObjectOpenHashMap<EquipSlot, List<Item>>(2)
                        val extras = Object2ObjectOpenHashMap<String, Any>(0)
                        while (nextEntry()) {
                            when (val key = key()) {
                                "levels" -> levels = string().toIntRange()
                                "inventory" -> {
                                    while (nextElement()) {
                                        val ids = ObjectArrayList<String>(1)
                                        var amount = 1
                                        while (nextEntry()) {
                                            when (key()) {
                                                "ids" -> while (nextElement()) {
                                                    ids.add(string())
                                                }
                                                "id" -> ids.add(string())
                                                "amount" -> amount = int()
                                            }
                                        }
                                        val items = ObjectArrayList<Item>(ids.size)
                                        for (item in ids) {
                                            items.add(Item(item, amount))
                                        }
                                        inventory.add(items)
                                    }
                                }
                                "equipment" -> {
                                    while (nextEntry()) {
                                        val slot = EquipSlot.valueOf(key().toSentenceCase())
                                        val items = items(1)
                                        equipment[slot] = items
                                    }
                                }
                                else -> extras[key] = value()
                            }
                        }
                        gear.add(GearDefinition(style, levels, equipment, inventory, extras = extras.ifEmpty { null }))
                    }
                    definitions[style] = gear
                }
            }
            this.definitions = definitions
            definitions.size
        }
        return this
    }

    private fun ConfigReader.items(expected: Int): List<Item> {
        val items = ObjectArrayList<Item>(expected)
        while (nextElement()) {
            items.add(item())
        }
        return items
    }

    private fun ConfigReader.item(): Item {
        var id = ""
        var amount = 1
        while (nextEntry()) {
            when (key()) {
                "id" -> id = string()
                "amount" -> amount = int()
            }
        }
        return Item(id, amount)
    }
}