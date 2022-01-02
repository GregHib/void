package world.gregs.voidps.cache.definition

@Suppress("UNCHECKED_CAST")
interface Extra {

    var stringId: String
    var extras: Map<String, Any>

    operator fun <T: Any> get(key: String): T = extras.getValue(key) as T

    fun has(key: String?) = extras.containsKey(key)

    fun <T : Any> getOrNull(key: String) = extras[key] as? T

    operator fun <T : Any> get(key: String, defaultValue: T) = getOrNull(key) as? T ?: defaultValue

}