package world.gregs.voidps.cache.definition.types

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.ConfigReader
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.cache.definition.type.NPCType

abstract class DefinitionTypes<T, D : Definition> : Types() {
    abstract val loaded: ByteArray
    abstract val ids: MutableMap<String, Int>
    abstract var extras: Array<Map<String, Any>?>

    abstract fun load(id: Int, definition: D)
    abstract fun save(id: Int, definition: D)

    fun load(definitions: Array<D>) {
        set(definitions.size)
        for (id in definitions.indices) {
            val definition = definitions[id]
            if (definition == NPCDefinition.EMPTY) {
                loaded[id] = 0
                continue
            }
            loaded[id] = 1
            load(id, definition)
        }
    }

    fun save(definitions: Array<D>) {
        for (id in definitions.indices) {
            if (loaded[id] == 0.toByte()) {
                continue
            }
            save(id, definitions[id])
        }
    }

    fun get(id: String) = NPCType(ids.getOrDefault(id, -1))

    fun getOrNull(id: String): T? {
        val index = ids.getOrDefault(id, -1)
        if (index == -1) {
            return null
        }
        return get(index)
    }

    abstract fun get(id: Int): T

    fun getOrNull(id: Int) = if (contains(id)) get(id) else null

    fun contains(id: Int) = loaded[id] == 1.toByte()

    internal fun getOrPutExtras(id: Int): MutableMap<String, Any> {
        var extras = this.extras[id]
        if (extras == null) {
            extras = Object2ObjectOpenHashMap(4, Hash.VERY_FAST_LOAD_FACTOR)
            this.extras[id] = extras
            return extras
        }
        return extras as MutableMap<String, Any>
    }

    override fun unloaded(reader: ConfigReader, key: String, value: Any, id: Int, section: String) {
        getOrPutExtras(id)[key] = value
    }
}