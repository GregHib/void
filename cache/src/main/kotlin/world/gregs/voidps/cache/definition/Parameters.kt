package world.gregs.voidps.cache.definition

/**
 * Interface for overriding adding [Parameterized] values into [Extra]
 */
interface Parameters {
    val parameters: Map<Int, String>

    fun set(extras: MutableMap<Int, Any>, id: Int, value: Any) {
        extras[id] = value
    }

    companion object {
        val EMPTY = object : Parameters {
            override val parameters: Map<Int, String> = emptyMap()
        }
    }
}
