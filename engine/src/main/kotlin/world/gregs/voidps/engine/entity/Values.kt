package world.gregs.voidps.engine.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.data.MapSerializer

/**
 * Map for storing a mix of temporary and general values
 */
@JsonSerialize(using = MapSerializer::class)
class Values(
    private val map: MutableMap<String, Any> = Object2ObjectOpenHashMap()
) : MutableMap<String, Any> by map {
    @JsonIgnore
    val temporary = Object2ObjectOpenHashMap<String, Any>()

    fun keys(): Set<String> = map.keys.union(temporary.keys)

    override fun get(key: String): Any? = map[key] ?: temporary[key]

    override fun containsKey(key: String): Boolean = map.containsKey(key) || temporary.containsKey(key)

    operator fun set(key: String, persistent: Boolean, value: Any) {
        if (persistent) {
            map[key] = value
        } else {
            temporary[key] = value
        }
    }

    override fun put(key: String, value: Any): Any? = if (map.containsKey(key)) {
        map.put(key, value)
    } else {
        temporary.put(key, value)
    }

    override fun remove(key: String): Any? = map.remove(key) ?: temporary.remove(key)

    override fun clear() {
        map.clear()
        temporary.clear()
    }
}

operator fun Entity.set(key: String, value: Any) {
    values[key] = value
}

operator fun Entity.set(key: String, persistent: Boolean, value: Any) {
    values[key, persistent] = value
}

@Suppress("UNCHECKED_CAST")
fun <T : Any> Entity.getOrPut(key: String, persistent: Boolean = false, block: () -> T): T {
    val value = getOrNull<T>(key)
    if (value == null) {
        val put = block()
        values[key, persistent] = put
        return put
    }
    return value
}

fun Entity.inc(key: String): Int {
    val value = get(key, 0) + 1
    values[key] = value
    return value
}

fun Entity.contains(key: String) = values.containsKey(key)

@Suppress("UNCHECKED_CAST")
operator fun <T : Any> Entity.get(key: String) = values[key] as T

@Suppress("UNCHECKED_CAST")
fun <T : Any> Entity.getOrNull(key: String): T? = values[key] as? T

operator fun <T : Any> Entity?.get(key: String, defaultValue: T) = if (this == null) defaultValue else getOrNull(key) as? T ?: defaultValue

@Suppress("UNCHECKED_CAST")
fun <T : Any> Entity.remove(key: String): T? = values.remove(key) as? T

fun Entity.clear(key: String) = values.remove(key) != null