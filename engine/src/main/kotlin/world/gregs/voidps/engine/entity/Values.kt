package world.gregs.voidps.engine.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.data.serial.MapSerializer

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