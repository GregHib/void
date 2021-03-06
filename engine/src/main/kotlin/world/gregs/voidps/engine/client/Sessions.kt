package world.gregs.voidps.engine.client

import org.koin.dsl.module
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.ClientSession
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
    val players = mutableMapOf<ClientSession, Player>()
    val channels = mutableMapOf<Player, ClientSession>()

    /**
     * Links a client session with a player
     */
    fun register(session: ClientSession, player: Player) {
        players[session] = player
        channels[player] = session
    }

    /**
     * Removes the link between a player an client session.
     */
    fun deregister(session: ClientSession) {
        channels.remove(players.remove(session))
    }

    /**
     * Returns player for [session]
     */
    fun get(session: ClientSession): Player? {
        return players[session]
    }

    /**
     * Returns session for [player]
     */
    fun get(player: Player): ClientSession? {
        return channels[player]
    }

    /**
     * Checks if [session] is linked
     */
    fun contains(session: ClientSession): Boolean {
        return players.containsKey(session)
    }

    /**
     * Checks if [player] is linked
     */
    fun contains(player: Player): Boolean {
        return channels.containsKey(player)
    }
}