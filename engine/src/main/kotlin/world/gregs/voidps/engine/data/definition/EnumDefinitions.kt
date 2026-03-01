package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import org.jetbrains.annotations.TestOnly
import world.gregs.config.Config
import world.gregs.voidps.cache.config.data.StructDefinition
import world.gregs.voidps.cache.definition.data.EnumDefinition
import world.gregs.voidps.cache.definition.data.EnumTypes
import world.gregs.voidps.engine.timedLoad
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

    fun stringOrNull(enum: String, key: String): String? {
        val definition = get(enum)
        val key = key(definition.keyType, key)
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
        val definition = get(enum)
        val key = key(definition.keyType, key)
        return definition.intOrNull(key)
    }

    fun item(enum: String, key: String): String {
        val definition = get(enum)
        assert(definition.valueType == EnumTypes.ITEM || definition.valueType == EnumTypes.ITEM_2) { "Enum $enum value type not Item, found: ${EnumTypes.name(definition.valueType)}" }
        val key = key(definition.keyType, key)
        return ItemDefinitions.get(definition.int(key)).stringId
    }

    fun tile(enum: String, key: String): Tile {
        val definition = get(enum)
        assert(definition.valueType == EnumTypes.TILE) { "Enum $enum value type not Tile, found: ${EnumTypes.name(definition.valueType)}" }
        val key = key(definition.keyType, key)
        return Tile(definition.int(key))
    }

    fun struct(enum: String, key: String): StructDefinition {
        val definition = get(enum)
        assert(definition.valueType == EnumTypes.TILE) { "Enum $enum value type not Tile, found: ${EnumTypes.name(definition.valueType)}" }
        val key = key(definition.keyType, key)
        return StructDefinitions.get(definition.int(key))
    }

    fun <T : Any> getStruct(id: String, index: Int, param: String): T {
        val enum = get(id)
        val struct = enum.int(index)
        return StructDefinitions.get(struct)[param]
    }

    fun <T : Any?> getStructOrNull(id: String, index: Int, param: String): T? {
        val enum = get(id)
        val struct = enum.int(index)
        return StructDefinitions.getOrNull(struct)?.getOrNull(param)
    }

    fun <T : Any> getStruct(id: String, index: Int, param: String, default: T): T {
        val enum = get(id)
        val struct = enum.int(index)
        return StructDefinitions.get(struct)[param, default]
    }

    fun load(list: List<String>): EnumDefinitions {
        timedLoad("enum extra") {
            require(ItemDefinitions.loaded) { "Item definitions must be loaded before enum definitions" }
            require(InterfaceDefinitions.loaded) { "Interface definitions must be loaded before enum definitions" }
            require(InventoryDefinitions.loaded) { "Inventory definitions must be loaded before enum definitions" }
            require(NPCDefinitions.loaded) { "NPC definitions must be loaded before enum definitions" }
            require(StructDefinitions.loaded) { "Struct definitions must be loaded before enum definitions" }
            require(ObjectDefinitions.loaded) { "Object definitions must be loaded before enum definitions" }
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
                        val extras = Object2ObjectOpenHashMap<String, Any>(2, Hash.VERY_FAST_LOAD_FACTOR)
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
                                        else -> key.toInt()
                                    }
                                    map[keyInt] = value()
                                }
                                else -> extras[key] = value()
                            }
                        }
                        if (id == -1) {
                            if (map.isNotEmpty()) {
                                custom.add(
                                    EnumDefinition(
                                        keyType = keyType,
                                        valueType = valueType,
                                        defaultString = defaultString,
                                        defaultInt = defaultInt,
                                        length = map.size,
                                        map = map,
                                        extras = extras,
                                        stringId = stringId,
                                    )
                                )
                            } else {
                                error("Enum '$stringId' has no values")
                            }
                        } else {
                            definitions[id].extras = extras
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
            this.ids = ids
            ids.size
        }
        return this
    }

    override fun empty() = EnumDefinition.EMPTY

}
