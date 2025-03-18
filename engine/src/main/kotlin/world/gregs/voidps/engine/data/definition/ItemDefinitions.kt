package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.pearx.kasechange.toSentenceCase
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.client.ui.chat.toIntRange
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.data.*
import world.gregs.voidps.engine.entity.character.player.equip.EquipType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.ItemKept
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class ItemDefinitions(
    override var definitions: Array<ItemDefinition>
) : DefinitionsDecoder<ItemDefinition> {

    val size: Int = definitions.size

    override lateinit var ids: Map<String, Int>

    override fun empty() = ItemDefinition.EMPTY

    fun load(path: String = Settings["definitions.items"]): ItemDefinitions {
        timedLoad("item extra") {
            val equipment = IntArray(definitions.size) { -1 }
            var index = 0
            for (def in definitions) {
                if (def.primaryMaleModel >= 0 || def.primaryFemaleModel >= 0) {
                    equipment[def.id] = index++
                }
            }
            val ids = Object2IntOpenHashMap<String>()
            ids.defaultReturnValue(-1)
            Config.fileReader(path, 256) {
                while (nextSection()) {
                    val stringId = section()
                    var id = -1
                    val extras = Object2ObjectOpenHashMap<String, Any>(4, Hash.VERY_FAST_LOAD_FACTOR)
                    while (nextPair()) {
                        when (val key = key()) {
                            "id" -> {
                                id = int()
                                if (id in equipment.indices && equipment[id] != -1) {
                                    extras["equip"] = equipment[id]
                                }
                            }
                            "slot" -> extras[key] = EquipSlot.valueOf(string())
                            "type" -> extras[key] = EquipType.valueOf(string())
                            "kept" -> extras[key] = ItemKept.valueOf(string())
                            "smelting" -> extras[key] = Smelting(this)
                            "smithing" -> extras[key] = Smithing(this)
                            "fishing" -> extras[key] = Catch(this)
                            "firemaking" -> extras[key] = Fire(this)
                            "mining" -> extras[key] = Ore(this)
                            "cooking" -> extras[key] = Uncooked(this)
                            "tanning" -> extras[key] = Tanning(this)
                            "spinning" -> extras[key] = Spinning(this)
                            "pottery" -> extras[key] = Pottery(this)
                            "weaving" -> extras[key] = Weaving(this)
                            "jewellery" -> extras[key] = Jewellery(this)
                            "silver_jewellery" -> extras[key] = Silver(this)
                            "runecrafting" -> extras[key] = Rune(this)
                            "cleaning" -> extras[key] = Cleaning(this)
                            "fletch_dart" -> extras[key] = FletchDarts(this)
                            "fletch_bolts" -> extras[key] = FletchBolts(this)
                            "fletching_unf" -> extras[key] = Fletching(this)
                            "light_source" -> extras[key] = LightSources(this)
                            "skill_req" -> {
                                val map = Object2IntOpenHashMap<Skill>(1, Hash.VERY_FAST_LOAD_FACTOR)
                                while (nextEntry()) {
                                    map[Skill.valueOf(key().toSentenceCase())] = int()
                                }
                                extras[key] = map
                            }
                            "heals" -> extras[key] = if (peek == '"') string().toIntRange() else {
                                val int = int()
                                int..int
                            }
                            "clone" -> {
                                val item = ids.getInt(string())
                                require(item != -1) { "Unable to find item id to clone '$item'" }
                                val definition = definitions[item]
                                extras.putAll(definition.extras ?: continue)
                            }
                            else -> extras[key] = value()
                        }
                    }
                    ids[stringId] = id
                    definitions[id].stringId = stringId
                    definitions[id].extras = extras
                }
            }
            for (definition in definitions) {
                if (definition.stringId.endsWith("_lent")) {
                    val normal = definitions[definition.lendId]
                    if (normal.extras != null) {
                        val lentExtras = Object2ObjectOpenHashMap(normal.extras)
                        lentExtras.remove("aka")
                        if (definition.extras != null) {
                            lentExtras.putAll(definition.extras!!)
                        }
                    }
                }
            }
            this.ids = ids
            ids.size
        }
        return this
    }
}