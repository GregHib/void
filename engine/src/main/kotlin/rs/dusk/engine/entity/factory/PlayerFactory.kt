package rs.dusk.engine.entity.factory

import rs.dusk.engine.data.PlayerLoader
import rs.dusk.engine.entity.event.Registered
import rs.dusk.engine.entity.model.Player
import rs.dusk.engine.event.EventBus
import rs.dusk.utility.inject

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
class PlayerFactory {

    private val loader: PlayerLoader by inject()
    private val bus: EventBus by inject()

    fun spawn(index: Int): Player {
        val player = loader.load("Test")
        player.id = index
        bus.emit(Registered(player))
        return player
    }
}