package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import org.jetbrains.annotations.TestOnly
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.data.EnumDefinition
import world.gregs.voidps.cache.definition.data.EnumTypes
import world.gregs.voidps.engine.timedLoad

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

    fun <T : Any> getStruct(id: String, index: Int, param: String): T {
        val enum = get(id)
        val struct = enum.getInt(index)
        return StructDefinitions.get(struct)[param]
    }

    fun <T : Any?> getStructOrNull(id: String, index: Int, param: String): T? {
        val enum = get(id)
        val struct = enum.getInt(index)
        return StructDefinitions.getOrNull(struct)?.getOrNull(param)
    }

    fun <T : Any> getStruct(id: String, index: Int, param: String, default: T): T {
        val enum = get(id)
        val struct = enum.getInt(index)
        return StructDefinitions.get(struct)[param, default]
    }

    fun load(list: List<String>): EnumDefinitions {
        timedLoad("enum extra") {
            require(ItemDefinitions.loaded) { "Item definitions must be loaded before enum definitions" }
            val ids = Object2IntOpenHashMap<String>(definitions.size, Hash.VERY_FAST_LOAD_FACTOR)
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
                                        EnumTypes.ITEM, EnumTypes.ITEM_2 -> ItemDefinitions.get(key).id
                                        else -> key.toInt()
                                    }
                                    map[keyInt] = value()
                                }
                                else -> extras[key] = value()
                            }
                        }
                        if (id == -1 && map.isNotEmpty()) {
                            val index = definitions.size
                            definitions = Array(index + 1) {
                                if (it == index) {
                                    EnumDefinition(
                                        it,
                                        keyType = keyType,
                                        valueType = valueType,
                                        defaultString = defaultString,
                                        defaultInt = defaultInt,
                                        length = map.size,
                                        map = map,
                                        extras = extras,
                                        stringId = stringId,
                                    )
                                } else {
                                    definitions[it]
                                }
                            }
                            ids[stringId] = index
                        } else {
                            definitions[id].extras = extras
                        }
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
