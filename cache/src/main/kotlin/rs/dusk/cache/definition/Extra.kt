package rs.dusk.cache.definition

interface Extra {

    var extras: Map<String, Any>

    operator fun get(key: String): Any = extras.getValue(key)

    fun getOrNull(key: String): Any? = extras[key]

    operator fun <T : Any> get(key: String, defaultValue: T) = getOrNull(key) as? T ?: defaultValue

}