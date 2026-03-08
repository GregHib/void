package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import org.jetbrains.annotations.TestOnly
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.timedLoad

object NPCDefinitions : DefinitionsDecoder<NPCDefinition> {

    override lateinit var ids: Map<String, Int>

    override fun empty() = NPCDefinition.EMPTY

    override lateinit var definitions: Array<NPCDefinition>

    var loaded = false
        private set

    fun init(definitions: Array<NPCDefinition>): NPCDefinitions {
        this.definitions = definitions
        loaded = true
        return this
    }

    @TestOnly
    fun set(definitions: Array<NPCDefinition>, map: Map<String, Int>) {
        this.definitions = definitions
        this.ids = map
        loaded = true
    }

    fun clear() {
        definitions = emptyArray()
        ids = emptyMap()
        loaded = false
    }

    fun load(
        paths: List<String>,
        dropTables: DropTables? = null,
    ): NPCDefinitions {
        timedLoad("npc config") {
            val ids = Object2IntOpenHashMap<String>()
            val refs = Object2IntOpenHashMap<String>()
            ids.defaultReturnValue(-1)
            for (path in paths) {
                refs.clear()
                Config.fileReader(path, 150) {
                    while (nextSection()) {
                        val stringId = section()
                        val params = Int2ObjectOpenHashMap<Any>(4, Hash.VERY_FAST_LOAD_FACTOR)
                        var id = -1
                        while (nextPair()) {
                            when (val key = key()) {
                                "clone" -> {
                                    val name = string()
                                    val npc = refs.getInt(name)
                                    require(npc >= 0) { "Cannot find npc to clone with id '$name' in ${path}. Make sure it's in the same file." }
                                    val definition = definitions[npc]
                                    params.putAll(definition.params ?: continue)
                                }
                                "id" -> id = int()
                                "categories" -> {
                                    val categories = ObjectLinkedOpenHashSet<String>(2, Hash.VERY_FAST_LOAD_FACTOR)
                                    while (nextElement()) {
                                        categories.add(string())
                                    }
                                    params[Params.CATEGORIES] = categories
                                }
                                "drop_table" -> {
                                    val table = string()
                                    require(dropTables == null || table.isBlank() || dropTables.get("${table}_drop_table") != null) { "Drop table '$table' not found for npc $stringId" }
                                    params[Params.DROP_TABLE] = table
                                }
                                else -> params[Params.id(key)] = value()
                            }
                        }
                        require(!ids.containsKey(stringId)) { "Duplicate npc id found '$stringId' at $path." }
                        refs[stringId] = id
                        ids[stringId] = id
                        definitions[id].stringId = stringId
                        if (params.isNotEmpty()) {
                            if (definitions[id].params != null) {
                                (definitions[id].params as MutableMap<Int, Any>).putAll(params)
                            } else {
                                definitions[id].params = params
                            }
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
