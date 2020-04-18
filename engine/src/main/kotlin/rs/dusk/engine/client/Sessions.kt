package rs.dusk.engine.client

import rs.dusk.core.network.model.message.Message
import rs.dusk.core.network.model.session.Session
import rs.dusk.engine.client.verify.ClientVerification
import rs.dusk.engine.entity.model.Player

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 31, 2020
 */
interface Sessions {

    /**
     * Links a client session with a player
     */
    fun register(session: Session, player: Player)

    /**
     * Removes the link between a player an client session.
     */
    fun deregister(session: Session)

    /**
     * Returns player for [session]
     */
    fun get(session: Session): Player?

    /**
     * Sends [message] to the player linked with [session] via [ClientVerification]
     */
    fun send(session: Session, message: Message)
}