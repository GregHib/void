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
    val sessions = mutableMapOf<Player, ClientSession>()

    /**
     * Links a client session with a player
     */
    fun register(session: ClientSession, player: Player) {
        sessions[player] = session
    }

    /**
     * Removes the link between a player an client session.
     */
    fun deregister(player: Player) {
        sessions.remove(player)
    }

    /**
     * Returns session for [player]
     */
    fun get(player: Player): ClientSession? {
        return sessions[player]
    }

    /**
     * Checks if [player] is linked
     */
    fun contains(player: Player): Boolean {
        return sessions.containsKey(player)
    }
}