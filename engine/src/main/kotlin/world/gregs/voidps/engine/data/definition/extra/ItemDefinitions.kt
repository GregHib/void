package world.gregs.voidps.engine.data.definition.extra

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.engine.client.ui.chat.toIntRange
import world.gregs.voidps.engine.data.definition.DefinitionsDecoder
import world.gregs.voidps.engine.data.definition.data.*
import world.gregs.voidps.engine.data.yaml.YamlParser
import world.gregs.voidps.engine.data.yaml.config.DefinitionConfig
import world.gregs.voidps.engine.data.yaml.parse.Parser
import world.gregs.voidps.engine.entity.character.player.equip.EquipType
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.ItemKept
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.network.visual.update.player.EquipSlot

class ItemDefinitions(
    decoder: ItemDecoder
) : DefinitionsDecoder<ItemDefinition> {

    override lateinit var definitions: Array<ItemDefinition>
    override lateinit var ids: Map<String, Int>

    val size = decoder.last

    init {
        val start = System.currentTimeMillis()
        definitions = decoder.indices.map { decoder.get(it) }.toTypedArray()
        timedLoad("item definition", definitions.size, start)
    }

    override fun empty() = ItemDefinition.EMPTY

    @Suppress("UNCHECKED_CAST")
    fun load(parser: YamlParser = get(), path: String = getProperty("itemDefinitionsPath")): ItemDefinitions {
        timedLoad("item extra") {
            val equipment = mutableMapOf<Int, Int>()
            var index = 0
            for (def in definitions) {
                if (def.primaryMaleModel >= 0 || def.primaryFemaleModel >= 0) {
                    equipment[def.id] = index++
                }
            }
            val ids = Object2IntOpenHashMap<String>()
            this.ids = ids
            val config = object : DefinitionConfig<ItemDefinition>(ids, definitions) {

                override fun setMapValue(parser: Parser, map: MutableMap<String, Any>, key: String, indent: Int, indentOffset: Int, withinMap: String?, parentMap: String?) {
                    if (indent > 1 && parentMap == "pottery") {
                        val value = parser.value(indentOffset, withinMap)
                        super.set(map, key, Pottery.Ceramic(value as Map<String, Any>), indent, parentMap)
                    } else {
                        super.setMapValue(parser, map, key, indent, indentOffset, withinMap, parentMap)
                    }
                }

                override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                    super.set(map, key, when (indent) {
                        0 -> when (key) {
                            "id" -> {
                                super.set(map, "equip", equipment.getOrDefault(value as Int, -1), indent, parentMap)
                                value
                            }
                            "slot" -> EquipSlot.valueOf(value as String)
                            "type" -> EquipType.valueOf(value as String)
                            "kept" -> ItemKept.valueOf(value as String)
                            else -> value
                        }
                        1 -> when (key) {
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
                            "heals" -> {
                                if (value is Int) value..value else if (value is String) value.toIntRange() else 0..0
                            }
                            else -> value
                        }
                        else -> when (key) {
                            "chance" -> (value as String).toIntRange()
                            "item" -> {
                                val id = value as String
                                Item(id, def = get(id))
                            }
                            else -> if (parentMap == "chances") {
                                (value as String).toIntRange()
                            } else {
                                value
                            }
                        }

                    }, indent, parentMap)
                }
            }
            parser.load<Any>(path, config)
            ids.size
        }
        return this
    }
}