package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import org.jetbrains.annotations.TestOnly
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.data.EnumDefinition
import world.gregs.voidps.engine.data.definition.ItemDefinitions.loaded
import world.gregs.voidps.engine.timedLoad

/**
 * Also known as DataMap in cs2 or tables
 */
object EnumDefinitions : DefinitionsDecoder<EnumDefinition> {

    override var definitions: Array<EnumDefinition> = emptyArray()

    override var ids: Map<String, Int> = emptyMap()

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

    fun load(path: String): EnumDefinitions {
        timedLoad("enum extra") {
            val ids = Object2IntOpenHashMap<String>(definitions.size, Hash.VERY_FAST_LOAD_FACTOR)
            Config.fileReader(path, 50) {
                while (nextSection()) {
                    val stringId = section()
                    var id = 0
                    val extras = Object2ObjectOpenHashMap<String, Any>(2, Hash.VERY_FAST_LOAD_FACTOR)
                    while (nextPair()) {
                        when (val key = key()) {
                            "id" -> {
                                id = int()
                                require(!ids.containsKey(stringId)) { "Duplicate enum id found '$stringId' at $path." }
                                ids[stringId] = id
                                definitions[id].stringId = stringId
                            }
                            else -> extras[key] = value()
                        }
                    }
                    definitions[id].extras = extras
                }
            }
            this.ids = ids
            ids.size
        }
        return this
    }

    override fun empty() = EnumDefinition.EMPTY
}
