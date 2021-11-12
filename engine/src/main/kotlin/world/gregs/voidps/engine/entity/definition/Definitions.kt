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

    fun contains(id: String): Boolean {
        if (extras.containsKey(id)) {
            return true
        }
        return getOrNull(id) != null
    }

    fun setExtras(definition: T, name: String, map: Map<String, Any>?) {
        map?.let {
            definition.extras = it
        }
        definition.stringId = name
    }

    fun getOrNull(id: Int): T? {
        val definition = decodeOrNull("", id) ?: return null
        val name = names[definition.id] ?: return null
        val (intId, map) = getIdAndExtras(name)
        if (intId == -1) {
            return null
        }
        setExtras(definition, name, map)
        return definition
    }

    fun getOrNull(id: String): T? {
        val (intId, map) = getIdAndExtras(id)
        if (intId == -1) {
            return null
        }
        val name = names[intId] ?: id
        val definition = decodeOrNull(name, intId) ?: return null
        setExtras(definition, name, map)
        return definition
    }

    fun get(id: Int): T {
        val definition = decode("", id)
        val name = names[definition.id] ?: definition.id.toString()
        val (_, map) = getIdAndExtras(name)
        setExtras(definition, name, map)
        return definition
    }

    fun get(id: String): T {
        val (intId, map) = getIdAndExtras(id)
        val name = names[intId] ?: id
        val definition = decode(name, intId)
        setExtras(definition, name, map)
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