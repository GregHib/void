package rs.dusk.engine.data

import org.koin.dsl.module
import rs.dusk.engine.entity.model.Player
import rs.dusk.engine.model.Tile
import rs.dusk.engine.model.Tile.Companion.add
import rs.dusk.utility.getProperty
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 03, 2020
 */
class PlayerLoader(strategy: StorageStrategy<Player>) : DataLoader<Player>(strategy) {

    private val x = getProperty("homeX", 0)
    private val y = getProperty("homeY", 0)
    private val plane = getProperty("homePlane", 0)
    private val tile = Tile(x, y, plane)
    val counter = AtomicInteger(0)// Temp

    override fun load(name: String): Player {
        return super.load(name) ?: Player(-1, tile.add(counter.getAndIncrement()))
    }
}

val playerLoaderModule = module {
    single { PlayerLoader(get()) }
}