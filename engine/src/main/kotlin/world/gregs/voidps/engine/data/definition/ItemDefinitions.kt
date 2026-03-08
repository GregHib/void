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
        timedLoad("item config") {
            val clones = Object2ObjectOpenHashMap<String, String>(100)
            val ids = Object2IntOpenHashMap<String>(18_000)
            ids.defaultReturnValue(-1)
            for (path in paths) {
                Config.fileReader(path, 256) {
                    while (nextSection()) {
                        val stringId = section()
                        var id = -1
                        val params = Int2ObjectOpenHashMap<Any>(4, Hash.VERY_FAST_LOAD_FACTOR)
                        while (nextPair()) {
                            when (val key = key()) {
                                "id" -> {
                                    id = int()
                                    if (definitions[id].params != null) {
                                        params.putAll(definitions[id].params!!)
                                    }
                                }
                                "slot" -> params[Params.SLOT] = EquipSlot.by(string())
                                "type" -> params[Params.TYPE] = EquipType.by(string())
                                "kept" -> params[Params.KEPT] = ItemKept.by(string())
                                "equip_req" -> {
                                    var i = 1
                                    while (nextEntry()) {
                                        params[Params.id("equip_skill_${i}")] = Skill.valueOf(key().toSentenceCase()).ordinal
                                        params[Params.id("equip_level_${i}")] = int()
                                        i++
                                    }
                                }
                                "skill_req" -> {
                                    var i = 1
                                    while (nextEntry()) {
                                        params[Params.id("use_skill_${i}")] = Skill.valueOf(key().toSentenceCase()).ordinal
                                        params[Params.id("use_level_${i}")] = int()
                                        i++
                                    }
                                }
                                "heals" -> params[Params.HEALS] = if (peek == '"') {
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
                                        params.putAll(definition.params ?: continue)
                                    }
                                }
                                "categories" -> {
                                    @Suppress("UNCHECKED_CAST")
                                    val categories = params.getOrPut(Params.CATEGORIES) { ObjectLinkedOpenHashSet<String>(4, Hash.VERY_FAST_LOAD_FACTOR) } as MutableSet<String>
                                    while (nextElement()) {
                                        categories.add(string())
                                    }
                                }
                                else -> params[Params.id(key)] = value()
                            }
                        }
                        require(!ids.containsKey(stringId)) { "Duplicate item id found '$stringId' at $path." }
                        ids[stringId] = id
                        loaded = true
                        definitions[id].stringId = stringId
                        if (params.size > 0) {
                            definitions[id].params = params
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
                val params = definitions[id].params as? MutableMap<Int, Any>
                if (params != null) {
                    for (param in definition.params ?: continue) {
                        if (param.key == Params.AKA) {
                            continue
                        }
                        if (!params.containsKey(param.key)) {
                            params[param.key] = param.value
                        }
                    }
                }
            }
            for (definition in definitions) {
                if (definition.stringId.endsWith("_lent")) {
                    val normal = definitions[definition.lendId]
                    if (normal.params != null) {
                        val lentParams = Object2ObjectOpenHashMap(normal.params)
                        lentParams.remove(Params.AKA)
                        val params = definition.params as? MutableMap<Int, Any>
                        if (params != null) {
                            lentParams.putAll(params)
                        }
                        definition.params = lentParams
                    }
                }
            }
            this.ids = ids
            ids.size
        }
        return this
    }
}
