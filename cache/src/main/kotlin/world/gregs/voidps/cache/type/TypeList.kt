package world.gregs.voidps.cache.type

interface TypeList<T: Type> {
    val types: Array<T?>

    fun empty(): T

    fun getOrNull(id: Int): T? = types[id]

    fun get(id: Int): T = getOrNull(id) ?: empty()

}