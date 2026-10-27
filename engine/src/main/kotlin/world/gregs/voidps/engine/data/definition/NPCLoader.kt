package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.cache.definition.decoder.NPCDecoder
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.load.DefinitionsState
import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader
import world.gregs.voidps.engine.data.snapshot.SnapshotFormat
import world.gregs.voidps.engine.data.snapshot.serializer.DefinitionsStateSerializer
import world.gregs.voidps.engine.entity.item.drop.DropTable
import world.gregs.voidps.engine.entity.item.drop.DropTablesLoader

object NPCLoader : Loader<DefinitionsState<NPCDefinition>> {
    override val name = "npcs"
    override val files = listOf("definitions.npcs")
    override val settings = listOf("world.members")
    override val usesCache = true
    override val dependsOn: List<Loader<*>> get() = listOf(DropTablesLoader)
    override val codec = SnapshotFormat.codec(DefinitionsStateSerializer(NPCDefinition::class, NPCDefinition.serializer(), default = -1))

    override fun load(context: LoadContext): DefinitionsState<NPCDefinition> {
        val definitions = NPCDecoder(Settings["world.members", false]).load(context.requireCache())
        return DefinitionsState(definitions, parse(definitions, context.paths(this), context[DropTablesLoader]))
    }

    override fun install(state: DefinitionsState<NPCDefinition>) = NPCDefinitions.install(state)

    fun parse(definitions: Array<NPCDefinition>, paths: List<String>, dropTables: Map<String, DropTable>?): Map<String, Int> {
        definitions.onEach { it.stringId = it.id.toString() }
        val clones = Object2ObjectOpenHashMap<String, String>(100)
        // Npcs which explicitly set a custom field, so deferred clones don't overwrite them
        val ids = Object2IntOpenHashMap<String>()
        ids.defaultReturnValue(-1)
        for (path in paths) {
            Config.fileReader(path, 150) {
                while (nextSection()) {
                    val stringId = section()
                    val params = Int2ObjectOpenHashMap<Any>(4, Hash.VERY_FAST_LOAD_FACTOR)
                    var id = -1
                    var hitpoints: Int? = null
                    var regenRate: Int? = null
                    var huntRange: Int? = null
                    var huntMode: String? = null
                    var allowedUnder: Boolean? = null
                    var solid: Boolean? = null
                    var blocksPlayers: Boolean? = null
                    while (nextPair()) {
                        when (val key = key()) {
                            "clone" -> {
                                val name = string()
                                val npcId = ids.getInt(name)
                                if (npcId == -1) {
                                    clones[stringId] = name
                                } else {
                                    val definition = definitions[npcId]
                                    hitpoints = hitpoints ?: definition.hitpoints
                                    regenRate = regenRate ?: definition.regenRate
                                    huntRange = huntRange ?: definition.huntRange
                                    huntMode = huntMode ?: definition.huntMode
                                    allowedUnder = allowedUnder ?: definition.allowedUnder
                                    solid = solid ?: definition.solid
                                    blocksPlayers = blocksPlayers ?: definition.blocksPlayers
                                    params.putAll(definition.params ?: continue)
                                }
                            }
                            "id" -> id = int()
                            "hitpoints" -> hitpoints = int()
                            "regen_rate_ticks" -> regenRate = int()
                            "hunt_range" -> huntRange = int()
                            "hunt_mode" -> huntMode = string()
                            "allowed_under" -> allowedUnder = boolean()
                            "solid" -> solid = boolean()
                            "blocks_players" -> blocksPlayers = boolean()
                            "categories" -> {
                                val categories = ObjectLinkedOpenHashSet<String>(2, Hash.VERY_FAST_LOAD_FACTOR)
                                while (nextElement()) {
                                    categories.add(string())
                                }
                                params[Params.CATEGORIES] = categories
                            }
                            "drop_table" -> {
                                val table = string()
                                require(dropTables == null || table.isBlank() || dropTables["${table}_drop_table"] != null) { "Drop table '$table' not found for npc $stringId" }
                                params[Params.DROP_TABLE] = table
                            }
                            else -> params[Params.id(key)] = value()
                        }
                    }
                    require(!ids.containsKey(stringId)) { "Duplicate npc id found '$stringId' at $path." }
                    ids[stringId] = id
                    require(definitions[id].stringId == id.toString()) { "Duplicate npc id found $id ${definitions[id].stringId} '$stringId' at $path." }
                    definitions[id].stringId = stringId
                    if (hitpoints != null) {
                        definitions[id].hitpoints = hitpoints
                    }
                    if (regenRate != null) {
                        definitions[id].regenRate = regenRate
                    }
                    if (huntRange != null) {
                        definitions[id].huntRange = huntRange
                    }
                    if (huntMode != null) {
                        definitions[id].huntMode = huntMode
                    }
                    if (allowedUnder != null) {
                        definitions[id].allowedUnder = allowedUnder
                    }
                    if (solid != null) {
                        definitions[id].solid = solid
                    }
                    if (blocksPlayers != null) {
                        definitions[id].blocksPlayers = blocksPlayers
                    }
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
        for ((npc, cloneStr) in clones) {
            val cloneId = ids.getInt(cloneStr)
            require(cloneId != -1) { "Unable to find npc id to clone '$cloneStr'" }
            val clone = definitions[cloneId]
            val id = ids.getInt(npc)
            require(id != -1) { "Unable to find npc id '$npc'" }
            val definition = definitions[id]
            if (definition.hitpoints == NPCDefinition.EMPTY.hitpoints) {
                definition.hitpoints = clone.hitpoints
            }
            if (definition.regenRate == NPCDefinition.EMPTY.regenRate) {
                definition.regenRate = clone.regenRate
            }
            if (definition.huntRange == NPCDefinition.EMPTY.huntRange) {
                definition.huntRange = clone.huntRange
            }
            if (definition.huntMode == NPCDefinition.EMPTY.huntMode) {
                definition.huntMode = clone.huntMode
            }
            if (definition.allowedUnder == NPCDefinition.EMPTY.allowedUnder) {
                definition.allowedUnder = clone.allowedUnder
            }
            if (definition.solid == NPCDefinition.EMPTY.solid) {
                definition.solid = clone.solid
            }
            if (definition.blocksPlayers == NPCDefinition.EMPTY.blocksPlayers) {
                definition.blocksPlayers = clone.blocksPlayers
            }
            val params = definition.params as? MutableMap<Int, Any> ?: continue
            for (param in clone.params ?: continue) {
                if (param.key == Params.AKA) {
                    continue
                }
                if (!params.containsKey(param.key)) {
                    params[param.key] = param.value
                }
            }
        }
        return ids
    }
}
