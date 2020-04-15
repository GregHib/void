package rs.dusk.engine.player

import org.koin.dsl.module
import rs.dusk.engine.entity.event.player.PlayerRequest
import rs.dusk.engine.event.EventBus
import rs.dusk.utility.inject

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
    single { PlayerControls() }
}