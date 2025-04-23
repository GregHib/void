package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import net.pearx.kasechange.toSentenceCase
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.client.ui.chat.toIntRange
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

    fun load(paths: List<String>): ItemDefinitions {
        timedLoad("item extra") {
            val clones = Object2ObjectOpenHashMap<String, String>(100)
            val ids = Object2IntOpenHashMap<String>(18_000)
            ids.defaultReturnValue(-1)
            for (path in paths) {
                Config.fileReader(path, 256) {
                    while (nextSection()) {
                        val stringId = section()
                        var id = -1
                        val extras = Object2ObjectOpenHashMap<String, Any>(4, Hash.VERY_FAST_LOAD_FACTOR)
                        while (nextPair()) {
                            when (val key = key()) {
                                "id" -> {
                                    id = int()
                                    if (definitions[id].extras != null) {
                                        extras.putAll(definitions[id].extras!!)
                                    }
                                }
                                "slot" -> extras[key] = EquipSlot.by(string())
                                "type" -> extras[key] = EquipType.by(string())
                                "kept" -> extras[key] = ItemKept.by(string())
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
                                    val item = string()
                                    val itemId = ids.getInt(item)
                                    if (itemId == -1) {
                                        clones[stringId] = item
                                    } else {
                                        val definition = definitions[itemId]
                                        extras.putAll(definition.extras ?: continue)
                                    }
                                }
                                "categories" -> {
                                    @Suppress("UNCHECKED_CAST")
                                    val categories = extras.getOrPut("categories") { ObjectLinkedOpenHashSet<String>(4, Hash.VERY_FAST_LOAD_FACTOR) } as MutableSet<String>
                                    while (nextElement()) {
                                        categories.add(string())
                                    }
                                }
                                else -> extras[key] = value()
                            }
                        }
                        require(!ids.containsKey(stringId)) { "Duplicate item id found '$stringId' at $path." }
                        ids[stringId] = id
                        definitions[id].stringId = stringId
                        if (extras.size > 0) {
                            definitions[id].extras = extras
                        }
                    }
                }
            }
            for ((item, clone) in clones) {
                val cloneId = ids.getInt(clone)
                require(cloneId != -1) { "Unable to find item id to clone '$clone'" }
                val definition = definitions[cloneId]
                val id = ids.getInt(item)
                require(id != -1) { "Unable to find item id '$item'" }
                val extras = definitions[id].extras as? MutableMap<String, Any>
                if (extras != null) {
                    for (extra in definition.extras ?: continue) {
                        if (!extras.containsKey(extra.key)) {
                            extras[extra.key] = extra.value
                        }
                    }
                }
            }
            for (definition in definitions) {
                if (definition.stringId.endsWith("_lent")) {
                    val normal = definitions[definition.lendId]
                    if (normal.extras != null) {
                        val lentExtras = Object2ObjectOpenHashMap(normal.extras)
                        lentExtras.remove("aka")
                        val extras = definition.extras as? MutableMap<String, Any>
                        if (extras != null) {
                            lentExtras.putAll(extras)
                        }
                        definition.extras = lentExtras
                    }
                }
            }
            this.ids = ids
            ids.size
        }
        return this
    }
}