package org.redrune.engine.entity.factory

import org.redrune.engine.entity.event.Registered
import org.redrune.engine.entity.model.Player
import org.redrune.engine.event.EventBus
import org.redrune.utility.inject

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
class PlayerFactory {

    private val bus: EventBus by inject()

    fun spawn(index: Int): Player {
        val player = Player(index)
        bus.emit(Registered(player))
        return player
    }
}