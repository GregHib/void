package world.gregs.voidps.network.game

import java.util.concurrent.ConcurrentHashMap

/**
 * Tracks the number of clients per ip address
 */
class ConnectionTracker {
    private val connections = ConcurrentHashMap<String, Int>()

    fun count(address: String) = connections[address] ?: 0

    fun add(address: String): Int? {
        connections[address] = count(address) + 1
        return null
    }

    fun remove(address: String) {
        val count = count(address) - 1
        if (count <= 0) {
            connections.remove(address)
        } else {
            connections[address] = count
        }
    }

    fun clear() {
        connections.clear()
    }
}