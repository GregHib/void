package world.gregs.voidps.cache.definition

interface Parameters {
    val parameters: Map<Int, String>

    fun set(extras: MutableMap<String, Any>, name: String, value: Any) {
        extras[name] = value
    }

    companion object {
        val EMPTY = object : Parameters {
            override val parameters: Map<Int, String> = emptyMap()
        }
    }
}