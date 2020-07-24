package rs.dusk.engine.data

import org.koin.dsl.module
import rs.dusk.engine.model.entity.character.player.Player
import rs.dusk.engine.model.world.Tile
import rs.dusk.utility.getProperty

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 03, 2020
 */
class PlayerLoader(strategy: StorageStrategy<Player>) : DataLoader<Player>(strategy) {

    private val x = getProperty("homeX", 0)
    private val y = getProperty("homeY", 0)
    private val plane = getProperty("homePlane", 0)
    private val tile = Tile(x, y, plane)

    fun loadPlayer(name: String): Player {
        return super.load(name) ?: Player(id = -1, tile = tile)
    }
}

val playerLoaderModule = module {
    single { PlayerLoader(get()) }
}