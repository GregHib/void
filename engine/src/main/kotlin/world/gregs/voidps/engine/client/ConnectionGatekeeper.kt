package world.gregs.voidps.engine.client

import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.network.NetworkGatekeeper
import java.util.concurrent.ConcurrentHashMap

/**
 * Keeps track of number of players online, prevents duplicate login attempts
 */
class ConnectionGatekeeper(
    players: Players
) : NetworkGatekeeper {

    private val online = ConcurrentHashMap.newKeySet<String>()
    private val logins = ConcurrentHashMap<String, Int>()
    private val indices = players.indexer

    override fun connections(address: String) = logins[address] ?: 0

    override fun connected(name: String) = online.contains(name)

    override fun connect(name: String, address: String?): Int? {
        online.add(name)
        if (address != null) {
            logins[address] = connections(address) + 1
        }
        return indices.obtain()
    }

    override fun disconnect(name: String, address: String) {
        online.remove(name)
        val count = connections(address) - 1
        if (count <= 0) {
            logins.remove(address)
        } else {
            logins[address] = count
        }
    }

    override fun releaseIndex(index: Int) {
        indices.release(index)
    }

    override fun clear() {
        online.clear()
        indices.clear()
        logins.clear()
    }
}