package world.gregs.voidps.cache.type

@Suppress("UNCHECKED_CAST")
interface Params {
    var stringId: String
    var params: Map<Int, Any>?

    operator fun <T : Any> get(key: Int): T = params!!.getValue(key) as T

    fun contains(key: Int) = params?.containsKey(key) ?: false

    fun <T : Any> getOrNull(key: Int) = params?.get(key) as? T

    operator fun <T : Any> get(key: Int, defaultValue: T) = getOrNull(key) as? T ?: defaultValue

    fun set(params: Map<Int, Any>) {
        if (this.params == null) {
            this.params = params
        } else {
            (this.params as MutableMap<Int, Any>).putAll(params)
        }
    }
}