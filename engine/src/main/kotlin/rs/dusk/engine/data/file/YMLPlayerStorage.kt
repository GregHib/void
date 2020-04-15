package rs.dusk.engine.data.file

import org.koin.dsl.module
import rs.dusk.engine.data.StorageStrategy
import rs.dusk.engine.entity.model.Player

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 03, 2020
 */
class YMLPlayerStorage(private val path: String, private val loader: FileLoader) : StorageStrategy<Player> {

    private fun path(name: String) = "$path\\$name.yml"

    override fun load(name: String): Player? {
        return loader.load(path(name))
    }

    override fun save(name: String, data: Player) {
        return loader.save(path(name), data)
    }
}

val ymlPlayerModule = module {
    single { YMLPlayerStorage(getProperty("savePath"), get()) }
}
