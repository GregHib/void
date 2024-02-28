package world.gregs.voidps.engine.client

import world.gregs.voidps.engine.client.ui.chat.toInt
import world.gregs.voidps.engine.entity.character.IndexAllocator
import world.gregs.voidps.network.SessionManager
import java.util.concurrent.ConcurrentHashMap

/**
 * Keeps track of the players online, prevents duplicate login attempts
 */
class LoginManager(
    private val indices: IndexAllocator
) : SessionManager {

    private val online = ConcurrentHashMap.newKeySet<String>()

    override fun count(key: String) = online.contains(key).toInt()

    override fun add(key: String): Int? {
        if (!online.add(key)) {
            return null
        }
        return indices.obtain()
    }

    override fun remove(key: String) {
        online.remove(key)
    }

    override fun clear() {
        indices.clear()
        online.clear()
    }
}