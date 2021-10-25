package world.gregs.voidps.engine.entity.definition

/**
 * Stores additional static information about an entity as well as a unique string identifier
 */
interface Extras {
    val extras: Map<String, Map<String, Any>>
    val names: Map<Int, String>

    fun getIdOrNull(intId: Int): String? {
        return names[intId]
    }

    fun getId(intId: Int): String = getIdOrNull(intId) ?: intId.toString()

    fun getIdOrNull(intId: String): String? {
        return getIdOrNull(intId.toIntOrNull() ?: return null)
    }

    fun getId(intId: String): String = getIdOrNull(intId)  ?: intId

    fun getIntIdOrNull(stringId: String): Int? {
        return stringId.toIntOrNull() ?: extras[stringId]?.get("id") as? Int
    }

    fun getIntId(stringId: String): Int {
        return getIntIdOrNull(stringId) ?: -1
    }
}