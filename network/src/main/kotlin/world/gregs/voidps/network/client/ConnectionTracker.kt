package world.gregs.voidps.network.client

import java.util.concurrent.ConcurrentHashMap

/**
 * Tracks the number of clients per ip address
 */
class ConnectionTracker(private val limit: Int) {
    private val connections = ConcurrentHashMap<String, Int>()

    fun add(address: String): Boolean {
        val current = connections[address] ?: 0
        if (current >= limit) {
            return false
        }
        connections[address] = current + 1
        return true
    }

    fun remove(address: String) {
        val count = connections[address] ?: 0
        if (count <= 1) {
            connections.remove(address)
        } else {
            connections[address] = count - 1
        }
    }

    fun clear() {
        connections.clear()
    }
}
