package world.gregs.voidps.cache.definition

interface Parameters {
    val parameters: Map<Int, String>

    fun set(extras: MutableMap<String, Any>, key: Int, value: Any) {
        val name = parameters.getOrDefault(key, key.toString())
        extras[name] = value
    }

    companion object {
        val EMPTY = object : Parameters {
            override val parameters: Map<Int, String> = emptyMap()
        }
    }
}