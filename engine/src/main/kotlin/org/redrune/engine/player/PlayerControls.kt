package org.redrune.engine.player

import org.koin.dsl.module
import org.redrune.engine.entity.event.player.PlayerRequest
import org.redrune.engine.event.EventBus
import org.redrune.utility.inject

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 31, 2020
 */
class PlayerControls : PlayerController {

    private val bus: EventBus by inject()

    override fun send(request: PlayerRequest) {
        bus.emit(request)
    }

}

val controlModule = module {
    single { PlayerControls() as PlayerController }
}