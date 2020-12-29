package rs.dusk.engine.client

import com.github.michaelbull.logging.InlineLogger
import com.google.common.collect.HashBiMap
import io.netty.channel.Channel
import org.koin.dsl.module
import rs.dusk.core.network.model.message.Message
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.utility.get

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
    val players = HashBiMap.create<Channel, Player>()

    /**
     * Links a client session with a player
     */
    fun register(session: Channel, player: Player) {
        players[session] = player
    }

    /**
     * Removes the link between a player an client session.
     */
    fun deregister(session: Channel) {
        players.remove(session)
    }

    /**
     * Returns player for [session]
     */
    fun get(session: Channel): Player? {
        return players[session]
    }

    /**
     * Returns session for [player]
     */
    fun get(player: Player): Channel? {
        return players.inverse()[player]
    }

    /**
     * Checks if [session] is linked
     */
    fun contains(session: Channel): Boolean {
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
    fun <T : Message> send(player: Player, message: T) {
        val session = get(player) ?: return// logger.debug { "Unable to find session for player $player." }
        session.writeAndFlush(message)
    }
}

fun <T : Message> Player.send(update: T) = get<Sessions>().send(this, update)