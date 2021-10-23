package world.gregs.voidps.engine.entity.definition

/**
 * Stores additional static information about an entity as well as a unique string identifier
 */
interface Extras {
    val extras: Map<String, Map<String, Any>>
    val names: Map<Int, String>

    fun getNameOrNull(id: Int): String? {
        return names[id]
    }

    fun getName(id: Int): String = getNameOrNull(id) ?: id.toString()

    fun getIdOrNull(name: String): Int? {
        return extras[name]?.get("id") as? Int
    }

    fun getId(name: String): Int {
        return getIdOrNull(name) ?: name.toIntOrNull() ?: -1
    }
}