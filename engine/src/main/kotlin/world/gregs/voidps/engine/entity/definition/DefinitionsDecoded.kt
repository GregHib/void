package world.gregs.voidps.engine.entity.definition

import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Extra
import world.gregs.voidps.engine.data.FileStorage

interface DefinitionsDecoded<D> where D : Definition, D : Extra {
    val definitions: Array<D>
    val ids: Map<String, Int>

    fun getOrNull(id: Int): D? {
        if (id == -1) {
            return null
        }
        return definitions[id]
    }

    fun empty(): D

    fun get(id: Int): D {
        return getOrNull(id) ?: empty()
    }

    fun getOrNull(id: String): D? {
        if (id.isBlank()) {
            return null
        }
        val int = id.toIntOrNull()
        if (int != null) {
            return getOrNull(int)
        }
        return getOrNull(ids[id] ?: return null)
    }

    fun get(id: String): D {
        return getOrNull(id) ?: empty()
    }

    fun contains(id: String): Boolean {
        return getOrNull(id) != null
    }

    fun apply(names: Map<Int, String>, extras: Map<String, Map<String, Any>>, block: (D) -> Unit = {}) {
        for (i in definitions.indices) {
            val definition = definitions[i]
            val name = names[i]
            definition.stringId = name ?: i.toString()
            val extra = extras[name] ?: continue
            definition.extras = extra
            block.invoke(definition)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun FileStorage.loadMapIds(path: String): Map<String, Map<String, Any>> = load<Map<String, Any>>(path).mapValues { (_, value) ->
        if (value is Int) mapOf("id" to value) else value as Map<String, Any>
    }
}