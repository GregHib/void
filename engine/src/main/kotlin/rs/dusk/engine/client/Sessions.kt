package rs.dusk.engine.client

import com.github.michaelbull.logging.InlineLogger
import com.google.common.collect.HashBiMap
import org.koin.dsl.module
import rs.dusk.core.network.model.message.Message
import rs.dusk.core.network.model.session.Session
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.utility.get
import kotlin.reflect.KClass

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 31, 2020
 */

@Suppress("USELESS_CAST")
val clientSessionModule = module {
    single { Sessions() }
}

class Sessions {

    private val logger = InlineLogger()
    val players = HashBiMap.create<Session, Player>()

    /**
     * Links a client session with a player
     */
    fun register(session: Session, player: Player) {
        players[session] = player
    }

    /**
     * Removes the link between a player an client session.
     */
    fun deregister(session: Session) {
        players.remove(session)
    }

    /**
     * Returns player for [session]
     */
    fun get(session: Session): Player? {
        return players[session]
    }

    /**
     * Returns session for [player]
     */
    fun get(player: Player): Session? {
        return players.inverse()[player]
    }

    /**
     * Checks if [session] is linked
     */
    fun contains(session: Session): Boolean {
        return players.containsKey(session)
    }

    /**
     * Checks if [player] is linked
     */
    fun contains(player: Player): Boolean {
        return players.inverse().containsKey(player)
    }

    /**
     * Sends [message] to the session linked with [player]
     */
    fun <T : Message> send(player: Player, clazz: KClass<T>, message: T) {
        val session = get(player) ?: return// logger.warn { "Unable to find session for player $player." }
        session.send(message)
    }
}

inline fun <reified T : Message> Player.send(update: T) = get<Sessions>().send(this, T::class, update)