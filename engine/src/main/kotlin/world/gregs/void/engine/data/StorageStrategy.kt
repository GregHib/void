package world.gregs.void.engine.data

/**
 * @author GregHib <greg@gregs.world>
 * @since April 03, 2020
 */
interface StorageStrategy<T : Any> {
    fun load(name: String): T?
    fun save(name: String, data: T)
}