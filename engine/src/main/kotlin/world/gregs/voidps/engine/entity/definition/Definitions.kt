package world.gregs.voidps.engine.entity.definition

import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Extra

/**
 * Stores additional static information about an entity as well as a unique string identifier
 */
interface Definitions<T> where T : Definition, T : Extra {
    val extras: Map<String, Map<String, Any>>
    val names: Map<Int, String>

    fun decodeOrNull(name: String, id: Int): T?

    fun decode(name: String, id: Int): T

    fun setExtras(definition: T, name: String, map: Map<String, Any>?) {
        map?.let {
            definition.extras = it
        }
        definition.stringId = name
    }

    fun getOrNull(id: Int): T? {
        return getOrNull(names[id] ?: return null)
    }

    fun getOrNull(id: String): T? {
        val (intId, map) = getIdAndExtras(id)
        if (intId == -1) {
            return null
        }
        val definition = decodeOrNull(id, intId) ?: return null
        setExtras(definition, id, map)
        return definition
    }

    fun get(id: Int): T = get(names[id] ?: id.toString())

    fun get(id: String): T {
        val (intId, map) = getIdAndExtras(id)
        val definition = decode(id, intId)
        setExtras(definition, id, map)
        return definition
    }

    private fun getIdAndExtras(id: String): Pair<Int, Map<String, Any>?> {
        var intId = id.toIntOrNull() ?: -1
        var map: Map<String, Any>? = null
        if (intId == -1) {
            map = extras[id]
            intId = map?.get("id") as? Int ?: -1
        } else {
            names[intId]?.let {
                map = extras[it]
            }
        }
        return intId to map
    }
}