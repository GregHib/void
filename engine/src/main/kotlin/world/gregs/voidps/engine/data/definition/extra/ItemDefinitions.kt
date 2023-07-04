package world.gregs.voidps.engine.data.definition.extra

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.data.definition.DefinitionsDecoder
import world.gregs.voidps.engine.data.definition.data.*
import world.gregs.voidps.engine.data.yaml.DefinitionConfig
import world.gregs.voidps.engine.entity.character.player.equip.EquipType
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.ItemKept
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.yaml.Yaml
import world.gregs.yaml.read.YamlReader

class ItemDefinitions(
    override var definitions: Array<ItemDefinition>
) : DefinitionsDecoder<ItemDefinition> {

    val size: Int = definitions.size

    override lateinit var ids: Map<String, Int>

    override fun empty() = ItemDefinition.EMPTY

    fun load(yaml: Yaml = get(), path: String = getProperty("itemDefinitionsPath")): ItemDefinitions {
        timedLoad("item extra") {
            val equipment = Int2IntOpenHashMap()
            var index = 0
            for (def in definitions) {
                if (def.primaryMaleModel >= 0 || def.primaryFemaleModel >= 0) {
                    equipment[def.id] = index++
                }
            }
            val ids = Object2IntOpenHashMap<String>()
            this.ids = ids
            val config = CustomConfig(equipment, ids, definitions, this)
            yaml.load<Any>(path, config)
            ids.size
        }
        return this
    }

    @Suppress("UNCHECKED_CAST")
    private class CustomConfig(private val equipment: Map<Int, Int>, ids: MutableMap<String, Int>, definitions: Array<ItemDefinition>, private val defs: ItemDefinitions) : DefinitionConfig<ItemDefinition>(ids, definitions) {
        override fun setMapValue(reader: YamlReader, map: MutableMap<String, Any>, key: String, indent: Int, indentOffset: Int, withinMap: String?, parentMap: String?) {
            if (indent > 1 && parentMap == "pottery") {
                val value = reader.value(indentOffset, withinMap)
                super.set(map, key, Pottery.Ceramic(value as Map<String, Any>), indent, parentMap)
            } else if (indent == 1 && key == "heals" || key == "chance" || parentMap == "chances") {
                set(map, key, reader.readIntRange(), indent, parentMap)
            } else {
                super.setMapValue(reader, map, key, indent, indentOffset, withinMap, parentMap)
            }
        }

        override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
            if (key == "<<") {
                map.putAll(value as Map<String, Any>)
                return
            }
            super.set(map, key, when (indent) {
                0 -> value
                1 -> when (key) {
                    "id" -> {
                        super.set(map, "equip", equipment.getOrDefault(value as Int, -1), indent, parentMap)
                        value
                    }
                    "slot" -> EquipSlot.valueOf(value as String)
                    "type" -> EquipType.valueOf(value as String)
                    "kept" -> ItemKept.valueOf(value as String)
                    "fishing" -> Catch(value as Map<String, Any>)
                    "firemaking" -> Fire(value as Map<String, Any>)
                    "mining" -> Ore(value as Map<String, Any>)
                    "cooking" -> Uncooked(value as Map<String, Any>)
                    "tanning" -> Tanning(value as List<List<Any>>)
                    "spinning" -> Spinning(value as Map<String, Any>)
                    "pottery" -> Pottery(value as Map<String, Pottery.Ceramic>)
                    "weaving" -> Weaving(value as Map<String, Any>)
                    "jewellery" -> Jewellery(value as Map<String, Any>)
                    "silver_jewellery" -> Silver(value as Map<String, Any>)
                    else -> value
                }
                else -> when (key) {
                    "item" -> {
                        val id = value as String
                        Item(id, def = defs.get(id))
                    }
                    else -> value
                }

            }, indent, parentMap)
        }

        private fun YamlReader.readIntRange(): IntRange {
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
}