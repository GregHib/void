package org.redrune.engine.entity.factory

import org.redrune.engine.data.PlayerLoader
import org.redrune.engine.entity.event.Registered
import org.redrune.engine.entity.model.Player
import org.redrune.engine.event.EventBus
import org.redrune.utility.inject

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