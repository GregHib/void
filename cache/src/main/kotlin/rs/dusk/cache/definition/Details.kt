package rs.dusk.cache.definition

interface Details {

    var details: Map<String, Any>

    operator fun get(key: String): Any = details.getValue(key)

    fun getOrNull(key: String): Any? = details[key]

    operator fun <T : Any> get(key: String, defaultValue: T) = getOrNull(key) as? T ?: defaultValue

}