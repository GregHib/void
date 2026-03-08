package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import net.pearx.kasechange.toSentenceCase
import org.jetbrains.annotations.TestOnly
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.client.ui.chat.toIntRange
import world.gregs.voidps.engine.entity.character.player.equip.EquipType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.ItemKept
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

object ItemDefinitions : DefinitionsDecoder<ItemDefinition> {

    override var definitions: Array<ItemDefinition> = emptyArray()

    var loaded = false
        private set

    val size: Int
        get() = definitions.size

    override var ids: Map<String, Int> = emptyMap()

    fun init(definitions: Array<ItemDefinition>): ItemDefinitions {
        this.definitions = definitions
        loaded = true
        return this
    }

    @TestOnly
    fun set(definitions: Array<ItemDefinition>, ids: Map<String, Int>) {
        this.definitions = definitions
        this.ids = ids
        loaded = true
    }

    fun clear() {
        this.definitions = emptyArray()
        this.ids = emptyMap()
        loaded = false
    }

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
                        val extras = Int2ObjectOpenHashMap<Any>(4, Hash.VERY_FAST_LOAD_FACTOR)
                        while (nextPair()) {
                            when (val key = key()) {
                                "id" -> {
                                    id = int()
                                    if (definitions[id].params != null) {
                                        extras.putAll(definitions[id].params!!)
                                    }
                                }
                                "slot" -> extras[Params.SLOT] = EquipSlot.by(string())
                                "type" -> extras[Params.TYPE] = EquipType.by(string())
                                "kept" -> extras[Params.KEPT] = ItemKept.by(string())
                                "equip_req" -> {
                                    var i = 1
                                    while (nextEntry()) {
                                        extras[Params.id("equip_skill_${i}")] = Skill.valueOf(key().toSentenceCase()).ordinal
                                        extras[Params.id("equip_level_${i}")] = int()
                                        i++
                                    }
                                }
                                "skill_req" -> {
                                    var i = 1
                                    while (nextEntry()) {
                                        extras[Params.id("use_skill_${i}")] = Skill.valueOf(key().toSentenceCase()).ordinal
                                        extras[Params.id("use_level_${i}")] = int()
                                        i++
                                    }
                                }
                                "heals" -> extras[Params.HEALS] = if (peek == '"') {
                                    string().toIntRange()
                                } else {
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
                                        extras.putAll(definition.params ?: continue)
                                    }
                                }
                                "categories" -> {
                                    @Suppress("UNCHECKED_CAST")
                                    val categories = extras.getOrPut(Params.CATEGORIES) { ObjectLinkedOpenHashSet<String>(4, Hash.VERY_FAST_LOAD_FACTOR) } as MutableSet<String>
                                    while (nextElement()) {
                                        categories.add(string())
                                    }
                                }
                                else -> extras[Params.id(key)] = value()
                            }
                        }
                        require(!ids.containsKey(stringId)) { "Duplicate item id found '$stringId' at $path." }
                        ids[stringId] = id
                        loaded = true
                        definitions[id].stringId = stringId
                        if (extras.size > 0) {
                            definitions[id].params = extras
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
                val extras = definitions[id].params as? MutableMap<Int, Any>
                if (extras != null) {
                    for (extra in definition.params ?: continue) {
                        if (extra.key == Params.AKA) {
                            continue
                        }
                        if (!extras.containsKey(extra.key)) {
                            extras[extra.key] = extra.value
                        }
                    }
                }
            }
            for (definition in definitions) {
                if (definition.stringId.endsWith("_lent")) {
                    val normal = definitions[definition.lendId]
                    if (normal.params != null) {
                        val lentExtras = Object2ObjectOpenHashMap(normal.params)
                        lentExtras.remove(Params.AKA)
                        val extras = definition.params as? MutableMap<Int, Any>
                        if (extras != null) {
                            lentExtras.putAll(extras)
                        }
                        definition.params = lentExtras
                    }
                }
            }
            this.ids = ids
            ids.size
        }
        return this
    }
}
