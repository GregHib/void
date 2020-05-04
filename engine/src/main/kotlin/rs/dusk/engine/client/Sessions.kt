package rs.dusk.engine.client

import rs.dusk.core.network.model.message.Message
import rs.dusk.core.network.model.session.Session
import rs.dusk.engine.client.verify.ClientVerification
import rs.dusk.engine.entity.event.player.ClientUpdate
import rs.dusk.engine.entity.model.Player
import kotlin.reflect.KClass

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 31, 2020
 */
abstract class Sessions {

    /**
     * Links a client session with a player
     */
    abstract fun register(session: Session, player: Player)

    /**
     * Removes the link between a player an client session.
     */
    abstract fun deregister(session: Session)

    /**
     * Checks if [session] is linked
     */
    abstract fun contains(session: Session): Boolean

    /**
     * Checks if [player] is linked
     */
    abstract fun contains(player: Player): Boolean

    /**
     * Returns player for [session]
     */
    abstract fun get(session: Session): Player?

    /**
     * Returns session for [player]
     */
    abstract fun get(player: Player): Session?

    /**
     * Sends [message] to the session linked with [player]
     */
    abstract fun <T : ClientUpdate> send(player: Player, clazz: KClass<T>, message: T)

    /**
     * Sends [message] to the player linked with [session] via [ClientVerification]
     */
    abstract fun <T : Message> send(session: Session, clazz: KClass<T>, message: T)

    inline fun <reified T : Message> send(session: Session, event: T) = send(session, T::class, event)
}