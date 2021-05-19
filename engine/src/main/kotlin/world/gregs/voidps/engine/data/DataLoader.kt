package world.gregs.voidps.engine.data

abstract class DataLoader<T : Any>(private val strategy: StorageStrategy<T>) {
    open fun load(name: String): T? {
        return strategy.load(name)
    }

    open fun save(name: String, data: T) {
        strategy.save(name, data)
    }
}