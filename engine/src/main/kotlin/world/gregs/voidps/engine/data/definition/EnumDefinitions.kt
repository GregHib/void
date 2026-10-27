package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import org.jetbrains.annotations.TestOnly
import world.gregs.config.Config
import world.gregs.voidps.cache.config.data.StructDefinition
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.cache.definition.data.EnumDefinition
import world.gregs.voidps.cache.definition.data.EnumTypes
import world.gregs.voidps.cache.definition.decoder.EnumDecoder
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.config.TableDefinition
import world.gregs.voidps.engine.data.load.DefinitionsCodec
import world.gregs.voidps.engine.data.load.DefinitionsState
import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader
import world.gregs.voidps.type.Tile

/**
 * Also known as DataMap in cs2 or tables
 */
object EnumDefinitions : DefinitionsDecoder<EnumDefinition> {

    override var definitions: Array<EnumDefinition> = emptyArray()

    override var ids: Map<String, Int> = emptyMap()

    var loaded = false

    fun init(definitions: Array<EnumDefinition>): EnumDefinitions {
        this.definitions = definitions
        loaded = true
        return this
    }

    @TestOnly
    fun set(definitions: Array<EnumDefinition>, ids: Map<String, Int>) {
        this.definitions = definitions
        this.ids = ids
        loaded = true
    }

    fun clear() {
        this.definitions = emptyArray()
        this.ids = emptyMap()
        loaded = false
    }

    private fun key(keyType: Char, key: String) = when (keyType) {
        EnumTypes.ITEM, EnumTypes.ITEM_2 -> ItemDefinitions.get(key).id
        EnumTypes.COMPONENT -> InterfaceDefinitions.get(key).id
        EnumTypes.INV -> InventoryDefinitions.get(key).id
        EnumTypes.NPC -> NPCDefinitions.get(key).id
        EnumTypes.STRUCT -> StructDefinitions.get(key).id
        EnumTypes.OBJ -> ObjectDefinitions.get(key).id
        EnumTypes.INT -> key.toInt()
        else -> error("Unsupported enum type: ${keyType.code}")
    }

    fun contains(enum: String, key: String): Boolean {
        val definition = get(enum)
        val key = key(definition.keyType, key)
        return definition.map?.contains(key) == true
    }

    fun string(enum: String, key: String): String {
        val definition = get(enum)
        val key = key(definition.keyType, key)
        return definition.string(key)
    }

    fun string(enum: String, key: Int): String {
        val definition = get(enum)
        return definition.string(key)
    }

    fun stringOrNull(enum: String, key: String): String? {
        val definition = getOrNull(enum) ?: return null
        val key = key(definition.keyType, key)
        return definition.stringOrNull(key)
    }

    fun tableOrNull(enum: String, key: String): TableDefinition? {
        val definition = getOrNull(enum) ?: return null
        val key = key(definition.keyType, key)
        val id = definition.stringOrNull(key) ?: return null
        return Tables.getOrNull(id)
    }

    fun rowOrNull(enum: String, key: String): RowDefinition? {
        val definition = getOrNull(enum) ?: return null
        val key = key(definition.keyType, key)
        val id = definition.stringOrNull(key) ?: return null
        return Rows.getOrNull(id)
    }

    fun stringOrNull(enum: String, key: Int): String? {
        val definition = getOrNull(enum) ?: return null
        return definition.stringOrNull(key)
    }

    fun int(enum: String, key: String): Int {
        val definition = get(enum)
        val key = key(definition.keyType, key)
        return definition.int(key)
    }

    fun int(enum: String, key: Int): Int {
        val definition = get(enum)
        return definition.int(key)
    }

    fun intOrNull(enum: String, key: String): Int? {
        val definition = getOrNull(enum) ?: return null
        val key = key(definition.keyType, key)
        return definition.intOrNull(key)
    }

    fun intOrNull(enum: String, key: Int): Int? {
        val definition = getOrNull(enum) ?: return null
        return definition.intOrNull(key)
    }

    fun item(enum: String, key: String): String {
        val definition = get(enum)
        require(definition.valueType == EnumTypes.ITEM || definition.valueType == EnumTypes.ITEM_2) { "Enum $enum value type not Item, found: ${EnumTypes.name(definition.valueType)}" }
        val key = key(definition.keyType, key)
        return ItemDefinitions.get(definition.int(key)).stringId
    }

    fun tile(enum: String, key: String): Tile {
        val definition = get(enum)
        require(definition.valueType == EnumTypes.TILE) { "Enum $enum value type not Tile, found: ${EnumTypes.name(definition.valueType)}" }
        val key = key(definition.keyType, key)
        return Tile(definition.int(key))
    }

    fun struct(enum: String, key: String): StructDefinition {
        val definition = get(enum)
        require(definition.valueType == EnumTypes.STRUCT) { "Enum $enum value type not Struct, found: ${EnumTypes.name(definition.valueType)}" }
        val key = key(definition.keyType, key)
        return StructDefinitions.get(definition.int(key))
    }

    fun <T : Any> getStruct(id: String, index: Int, param: String): T {
        val enum = get(id)
        val struct = enum.int(index)
        return StructDefinitions.get(struct)[param]
    }

    fun <T> getStructOrNull(id: String, index: Int, param: String): T? {
        val enum = get(id)
        val struct = enum.int(index)
        return StructDefinitions.getOrNull(struct)?.getOrNull(param)
    }

    fun <T : Any> getStruct(id: String, index: Int, param: String, default: T): T {
        val enum = get(id)
        val struct = enum.int(index)
        return StructDefinitions.get(struct)[param, default]
    }

    fun install(state: DefinitionsState<EnumDefinition>) {
        definitions = state.definitions
        ids = state.ids
        loaded = true
    }

    /**
     * Applies the configs in [list] to the already decoded [definitions].
     */
    fun load(list: List<String>): EnumDefinitions {
        val state = EnumLoader.parse(definitions, list)
        definitions = state.definitions
        ids = state.ids
        return this
    }

    override fun empty() = EnumDefinition.EMPTY
}

object EnumLoader : Loader<DefinitionsState<EnumDefinition>> {
    override val name = "enums"
    override val files = listOf("definitions.enums")
    override val usesCache = true
    override val dependsOn: List<Loader<*>> get() = listOf(ItemLoader, InterfaceLoader, InventoryLoader, NPCLoader, StructLoader, ObjectLoader, TableLoader)
    override val codec = DefinitionsCodec(
        EnumDefinition::class.java,
        default = null,
        writeDefinition = { definition ->
            int(definition.id)
            int(definition.keyType.code)
            int(definition.valueType.code)
            string(definition.defaultString)
            int(definition.defaultInt)
            int(definition.length)
            val map = definition.map
            if (map == null) {
                int(-1)
            } else {
                int(map.size)
                // Config defined enums keep their order
                boolean(map is LinkedHashMap<*, *>)
                for ((key, value) in map) {
                    int(key)
                    value(value)
                }
            }
            string(definition.stringId)
            params(definition.params)
        },
        readDefinition = {
            val id = int()
            val keyType = int().toChar()
            val valueType = int().toChar()
            val defaultString = string()
            val defaultInt = int()
            val length = int()
            val size = int()
            val map: Map<Int, Any>? = if (size == -1) {
                null
            } else {
                val ordered = boolean()
                val map: MutableMap<Int, Any> = if (ordered) LinkedHashMap(size) else Int2ObjectOpenHashMap(size, Hash.VERY_FAST_LOAD_FACTOR)
                repeat(size) {
                    val key = int()
                    map[key] = value()!!
                }
                map
            }
            EnumDefinition(id, keyType, valueType, defaultString, defaultInt, length, map, string(), params())
        },
    )

    override fun load(context: LoadContext): DefinitionsState<EnumDefinition> = parse(EnumDecoder().load(context.requireCache()), context.paths(this))

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
