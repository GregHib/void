package world.gregs.voidps.engine.entity.character.player.login

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.dsl.module
import world.gregs.voidps.engine.entity.list.MAX_PLAYERS
import world.gregs.voidps.utility.getIntProperty
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque
import kotlin.coroutines.resume

/**
 * @author GregHib <greg@gregs.world>
 * @since March 31, 2020
 */
val loginQueueModule = module {
    single {
        LoginQueue(getIntProperty("loginPerTickCap", 1))
    }
}

/**
 * Keeps track of number of players online, prevents duplicate login attempts
 * Loads player save files in the background, queueing once successful
 * Each tick at the correct time, accepts the first [loginPerTickCap] players into the world.
 */
class LoginQueue(
    private val loginPerTickCap: Int,
) : Runnable {

    private val online = ConcurrentHashMap.newKeySet<String>()
    private val indices = ConcurrentLinkedDeque((1 until MAX_PLAYERS).toList())
    private val waiting = ConcurrentHashMap.newKeySet<CancellableContinuation<Unit>>()

    fun isOnline(name: String) = online.contains(name)

    fun login(name: String): Int? {
        online.add(name)
        return indices.poll()
    }

    fun logout(name: String, index: Int?) {
        online.remove(name)
        if (index != null) {
            indices.add(index)
        }
    }

    suspend fun await() = suspendCancellableCoroutine<Unit> {
        waiting.add(it)
    }

    override fun run() {
        val iterator = waiting.iterator()
        var count = 0
        while (iterator.hasNext() && count++ < loginPerTickCap) {
            val next = iterator.next()
            next.resume(Unit)
            iterator.remove()
        }
    }
}