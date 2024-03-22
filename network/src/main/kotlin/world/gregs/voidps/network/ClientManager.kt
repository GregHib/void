package world.gregs.voidps.network

import java.util.concurrent.ConcurrentHashMap

/**
 * Keeps track of number of clients per ip address
 */
class ClientManager {
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