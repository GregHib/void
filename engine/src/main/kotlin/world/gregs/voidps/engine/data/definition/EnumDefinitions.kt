package world.gregs.voidps.engine.data.definition

import org.jetbrains.annotations.TestOnly
import world.gregs.voidps.cache.config.data.StructDefinition
import world.gregs.voidps.cache.definition.data.EnumDefinition
import world.gregs.voidps.cache.definition.data.EnumTypes
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.config.TableDefinition
import world.gregs.voidps.engine.data.load.DefinitionsState
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
