package world.gregs.voidps.cache.type.field

interface AccessibleField<T> : Field {
    fun get(index: Int): T

    fun set(index: Int, value: T)
}
