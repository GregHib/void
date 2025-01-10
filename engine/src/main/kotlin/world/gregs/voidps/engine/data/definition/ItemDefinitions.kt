package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.data.*
import world.gregs.voidps.engine.data.yaml.DefinitionConfig
import world.gregs.voidps.engine.entity.character.player.equip.EquipType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.ItemKept
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.yaml.Yaml
import world.gregs.yaml.read.YamlReader

class ItemDefinitions(
    override var definitions: Array<ItemDefinition>
) : DefinitionsDecoder<ItemDefinition> {

    val size: Int = definitions.size

    override lateinit var ids: Map<String, Int>

    override fun empty() = ItemDefinition.EMPTY

    fun load(yaml: Yaml = get(), path: String = Settings["definitions.items"]): ItemDefinitions {
        timedLoad("item extra") {
            val equipment = IntArray(definitions.size) { -1 }
            var index = 0
            for (def in definitions) {
                if (def.primaryMaleModel >= 0 || def.primaryFemaleModel >= 0) {
                    equipment[def.id] = index++
                }
            }
            val ids = Object2IntOpenHashMap<String>()
            this.ids = ids
            val config = CustomConfig(equipment, ids, definitions)
            yaml.load<Any>(path, config)
            ids.size
        }
        return this
    }

    @Suppress("UNCHECKED_CAST")
    private class CustomConfig(
        private val equipment: IntArray,
        ids: MutableMap<String, Int>,
        definitions: Array<ItemDefinition>
    ) : DefinitionConfig<ItemDefinition>(ids, definitions) {
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

        override fun set(map: MutableMap<String, Any>, key: String, id: Int, extras: Map<String, Any>?) {
            if (key.endsWith("_lent") && id in definitions.indices) {
                val def = definitions[id]
                val normal = definitions[def.lendId]
                if (normal.extras != null) {
                    val lentExtras = Object2ObjectOpenHashMap(normal.extras)
                    lentExtras.remove("aka")
                    if (extras != null) {
                        lentExtras.putAll(extras)
                    }
                    super.set(map, key, id, lentExtras)
                } else {
                    super.set(map, key, id, extras)
                }
            } else {
                super.set(map, key, id, extras)
            }
        }

        override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
            if (indent == 1) {
                super.set(map, key, when (key) {
                    "<<" -> {
                        map.putAll(value as Map<String, Any>)
                        return
                    }
                    "id" -> {
                        value as Int
                        if (value in equipment.indices && equipment[value] != -1) {
                            super.set(map, "equip", equipment[value], indent, parentMap)
                        }
                        value
                    }
                    "slot" -> EquipSlot.valueOf(value as String)
                    "type" -> EquipType.valueOf(value as String)
                    "kept" -> ItemKept.valueOf(value as String)
                    "smelting" -> Smelting(value as Map<String, Any>)
                    "smithing" -> Smithing(value as Map<String, Any>)
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
                    "runecrafting" -> Rune(value as Map<String, Any>)
                    "ammo" -> ObjectOpenHashSet(value as List<String>)
                    "cleaning" -> Cleaning(value as Map<String, Any>)
                    "fletch_dart" -> FletchDarts(value as Map<String, Any>)
                    "fletch_bolts" -> FletchBolts(value as Map<String, Any>)
                    "fletching_unf" -> Fletching(value as Map<String, Any>)
                    "light_source" -> LightSources(value as Map<String, Any>)
                    "skill_req" -> (value as MutableMap<String, Any>).mapKeys { Skill.valueOf(it.key.toSentenceCase()) }
                    else -> value
                }, indent, parentMap)
            } else {
                super.set(map, key, value, indent, parentMap)
            }
        }

        override fun anchor(anchor: Any): Any {
            val value = super.anchor(anchor)
            if (value is MutableMap<*, *>) {
                value.remove("aka")
            }
            return value
        }
    }
}