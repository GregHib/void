package world.gregs.voidps.engine.client

import world.gregs.voidps.engine.entity.list.MAX_PLAYERS
import world.gregs.voidps.network.NetworkGatekeeper
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque

/**
 * Keeps track of number of players online, prevents duplicate login attempts
 */
class ConnectionGatekeeper : NetworkGatekeeper {

    private val online = ConcurrentHashMap.newKeySet<String>()
    private val indices = ConcurrentLinkedDeque((1 until MAX_PLAYERS).toList())
    private val logins = ConcurrentHashMap<String, Int>()

    override fun connections(address: String) = logins[address] ?: 0

    override fun connected(name: String) = online.contains(name)

    override fun connect(name: String, address: String?): Int? {
        online.add(name)
        if (address != null) {
            logins[address] = connections(address) + 1
        }
        return indices.poll()
    }

    override fun disconnect(name: String, address: String, index: Int?) {
        online.remove(name)
        val count = connections(address) - 1
        if (count <= 0) {
            logins.remove(address)
        } else {
            logins[address] = count
        }
        if (index != null) {
            indices.add(index)
        }
    }
}