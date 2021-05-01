package world.gregs.voidps.engine.entity.character

/**
 * Map for storing any temporary values
 */
class CharacterValues {
    private val map = HashMap<String, Any>()
    private val persistent = mutableSetOf<String>()

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String, defaultValue: T): T {
        return (map[key] as? T) ?: defaultValue
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(key: String) = map[key] as T

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getOrNull(key: String) = map[key] as? T

    fun has(key: String) = map.containsKey(key)

    fun getBoolean(key: String) = get(key, false)

    fun getLong(key: String) = get(key, -1L)

    fun getInt(key: String) = get(key, -1)

    fun getDouble(key: String) = get(key, -1.0)

    fun getString(key: String) = get(key, "")

    operator fun set(key: String, value: Any) {
        map[key] = value
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> remove(key: String): T? = map.remove(key) as? T

    fun clear(key: String) {
        map.remove(key)
    }

    fun persist(key: String) = persistent.add(key)
}

operator fun Character.set(key: String, value: Any) {
    values[key] = value
}

operator fun Character.set(key: String, persistent: Boolean, value: Any) {
    values[key] = value
    if (persistent) {
        values.persist(key)
    }
}

fun Character.inc(key: String): Int {
    val value = get(key, 0) + 1
    values[key] = value
    return value
}

fun Character.contains(key: String) = values.has(key)

operator fun <T : Any> Character.get(key: String) = values.get<T>(key)

operator fun <T : Any> Character.get(key: String, defaultValue: T) = values.get(key, defaultValue)

fun <T : Any> Character.getOrNull(key: String): T? = values.getOrNull(key)

fun <T : Any> Character.remove(key: String): T? = values.remove(key)

fun Character.clear(key: String) = values.clear(key)