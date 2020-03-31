package org.redrune.engine.player

import org.koin.dsl.module
import org.redrune.engine.entity.event.player.PlayerUpdate
import org.redrune.engine.entity.model.Player
import org.redrune.utility.get

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
    single { Transmitter() as ClientTransmitter }
}

fun Player.send(update: PlayerUpdate) = get<ClientTransmitter>().send(update)