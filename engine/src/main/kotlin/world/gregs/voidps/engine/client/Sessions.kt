package world.gregs.voidps.engine.client

import io.netty.channel.Channel
import org.koin.dsl.module
import world.gregs.voidps.engine.entity.character.player.Player
import kotlin.collections.set

/**
 * @author GregHib <greg@gregs.world>
 * @since March 31, 2020
 */

@Suppress("USELESS_CAST")
val clientSessionModule = module {
    single { Sessions() }
}

class Sessions {
    val players = mutableMapOf<Channel, Player>()
    val channels = mutableMapOf<Player, Channel>()

    /**
     * Links a client session with a player
     */
    fun register(session: Channel, player: Player) {
        players[session] = player
        channels[player] = session
    }

    /**
     * Removes the link between a player an client session.
     */
    fun deregister(session: Channel) {
        channels.remove(players.remove(session))
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
        return channels[player]
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
        return channels.containsKey(player)
    }
}