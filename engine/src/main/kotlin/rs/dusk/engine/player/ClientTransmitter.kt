package rs.dusk.engine.player

import rs.dusk.engine.entity.event.player.PlayerUpdate

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 31, 2020
 */
interface ClientTransmitter {

    fun send(update: PlayerUpdate)

}