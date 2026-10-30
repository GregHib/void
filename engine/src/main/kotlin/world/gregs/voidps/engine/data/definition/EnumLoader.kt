package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.cache.definition.data.EnumDefinition
import world.gregs.voidps.cache.definition.data.EnumTypes
import world.gregs.voidps.cache.definition.decoder.EnumDecoder
import world.gregs.voidps.engine.data.load.DefinitionsState
import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader
import world.gregs.voidps.engine.data.snapshot.SnapshotFormat
import world.gregs.voidps.engine.data.snapshot.serializer.DefinitionsStateSerializer

object EnumLoader : Loader<DefinitionsState<EnumDefinition>> {
    override val name = "enums"
    override val label = "enum config"
    override val files = listOf("definitions.enums")
    override val usesCache = true
    override val dependsOn: List<Loader<*>> get() = listOf(ItemLoader, InterfaceLoader, InventoryLoader, NPCLoader, StructLoader, ObjectLoader, TableLoader)
    override val codec = SnapshotFormat.codec(DefinitionsStateSerializer(EnumDefinition::class, EnumDefinition.serializer(), default = null))

    override fun load(context: LoadContext): DefinitionsState<EnumDefinition> = parse(EnumDecoder().load(context.requireCache()), context.paths(this))

    override fun total(state: DefinitionsState<EnumDefinition>) = state.ids.size

    override fun install(state: DefinitionsState<EnumDefinition>) = EnumDefinitions.install(state)

    fun parse(decoded: Array<EnumDefinition>, list: List<String>): DefinitionsState<EnumDefinition> {
        var definitions = decoded
        val ids = Object2IntOpenHashMap<String>(definitions.size, Hash.VERY_FAST_LOAD_FACTOR)
        val custom = mutableListOf<EnumDefinition>()
        for (path in list) {
            Config.fileReader(path, 250) {
                while (nextSection()) {
                    val stringId = section()
                    var id = -1
                    var keyType: Char = 0.toChar()
                    var valueType: Char = 0.toChar()
                    var defaultString = "null"
                    var defaultInt = 0
                    val params = Int2ObjectOpenHashMap<Any>(2, Hash.VERY_FAST_LOAD_FACTOR)
                    val map = mutableMapOf<Int, Any>()
                    while (nextPair()) {
                        when (val key = key()) {
                            "id" -> {
                                id = int()
                                require(!ids.containsKey(stringId)) { "Duplicate enum id found '$stringId' at $path." }
                                ids[stringId] = id
                                definitions[id].stringId = stringId
                            }
                            "keyType" -> {
                                val string = string()
                                keyType = EnumTypes.char(string) ?: error("Unknown enum type: $string")
                            }
                            "valueType" -> {
                                val string = string()
                                valueType = EnumTypes.char(string) ?: error("Unknown enum type: $string")
                            }
                            "defaultString" -> defaultString = string()
                            "defaultInt" -> defaultInt = int()
                            "values" -> while (nextEntry()) {
                                val key = key()
                                val keyInt = when (keyType) {
                                    EnumTypes.ITEM, EnumTypes.ITEM_2 -> ItemDefinitions.getOrNull(key)?.id ?: error("Unknown item '$key' ${exception()}")
                                    EnumTypes.COMPONENT -> InterfaceDefinitions.getOrNull(key)?.id ?: error("Unknown interface '$key' ${exception()}")
                                    EnumTypes.INV -> InventoryDefinitions.getOrNull(key)?.id ?: error("Unknown inventory '$key' ${exception()}")
                                    EnumTypes.NPC -> NPCDefinitions.getOrNull(key)?.id ?: error("Unknown npc '$key' ${exception()}")
                                    EnumTypes.STRUCT -> StructDefinitions.getOrNull(key)?.id ?: error("Unknown struct '$key' ${exception()}")
                                    EnumTypes.OBJ -> ObjectDefinitions.getOrNull(key)?.id ?: error("Unknown struct '$key' ${exception()}")
                                    EnumTypes.DBROW -> Rows.getOrNull(key)?.id ?: error("Unknown table row '$key' ${exception()}")
                                    else -> key.toInt()
                                }
                                map[keyInt] = value()
                            }
                            else -> params[Params.id(key)] = value()
                        }
                    }
                    if (id == -1) {
                        custom.add(
                            EnumDefinition(
                                keyType = keyType,
                                valueType = valueType,
                                defaultString = defaultString,
                                defaultInt = defaultInt,
                                length = map.size,
                                map = map,
                                params = params,
                                stringId = stringId,
                            ),
                        )
                    } else {
                        definitions[id].params = params
                    }
                }
            }
        }
        if (custom.isNotEmpty()) {
            val index = definitions.size
            definitions = Array(index + custom.size) { i ->
                if (i >= index) {
                    custom[i - index].also {
                        ids[it.stringId] = i
                        it.id = i
                    }
                } else {
                    definitions[i]
                }
            }
        }
        return DefinitionsState(definitions, ids)
    }
}
