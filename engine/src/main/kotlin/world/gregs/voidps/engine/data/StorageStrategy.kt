package world.gregs.voidps.engine.data

interface StorageStrategy<T : Any> {
    fun load(name: String): T?
    fun save(name: String, data: T)
}