package rs.dusk.engine.player

import org.koin.dsl.module
import rs.dusk.engine.entity.event.player.PlayerUpdate
import rs.dusk.engine.entity.model.Player
import rs.dusk.utility.get

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 31, 2020
 */
class Transmitter : ClientTransmitter {

    override fun send(update: PlayerUpdate) {
        TODO("Not yet implemented")
    }

}

val transmitterModule = module {
    single { Transmitter() }
}

fun Player.send(update: PlayerUpdate) = get<ClientTransmitter>().send(update)