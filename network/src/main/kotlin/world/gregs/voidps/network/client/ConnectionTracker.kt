package world.gregs.voidps.network.client

import java.util.concurrent.ConcurrentHashMap

/**
 * Tracks the number of clients per ip address
 */
class ConnectionTracker(private val limit: Int) {
    private val connections = ConcurrentHashMap<String, Int>()

    fun add(address: String): Boolean {
        var added = false
        connections.compute(address) { _, current ->
            val count = current ?: 0
            if (count >= limit) {
                current
            } else {
                added = true
                count + 1
            }
        }
        return added
    }

    fun remove(address: String) {
        connections.computeIfPresent(address) { _, count -> if (count <= 1) null else count - 1 }
    }

    fun clear() {
        connections.clear()
    }
}
