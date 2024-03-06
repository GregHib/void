package world.gregs.voidps.engine.client

import world.gregs.voidps.network.SessionManager
import java.util.concurrent.ConcurrentHashMap

/**
 * Keeps track of number of clients per ip address
 */
class ClientManager : SessionManager {
    private val connections = ConcurrentHashMap<String, Int>()

    override fun count(key: String) = connections[key] ?: 0

    override fun add(key: String): Int? {
        connections[key] = count(key) + 1
        return null
    }

    override fun remove(key: String) {
        val count = count(key) - 1
        if (count <= 0) {
            connections.remove(key)
        } else {
            connections[key] = count
        }
    }

    override fun clear() {
        connections.clear()
    }
}