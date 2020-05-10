package rs.dusk.engine.client

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.koin.dsl.module
import rs.dusk.core.network.model.session.Session
import rs.dusk.engine.EngineTasks
import rs.dusk.engine.entity.factory.PlayerFactory
import rs.dusk.utility.inject
import java.util.concurrent.ConcurrentLinkedDeque
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 31, 2020
 */
@Suppress("USELESS_CAST")
val clientLoginQueueModule = module {
    single { PlayerLoginQueue(get(), getProperty("loginPerTickCap")) as LoginQueue }
}

class PlayerLoginQueue(tasks: EngineTasks, private val loginPerTickCap: Int) : LoginQueue(tasks, 10) {

    private val factory: PlayerFactory by inject()
    private val logger = InlineLogger()
    val queue = ConcurrentLinkedDeque<Continuation<Unit>>()

    override fun add(username: String, session: Session?) = GlobalScope.async {
        suspendCoroutine<Unit> {
            queue.add(it)
        }

        try {
            val player = factory.spawn(username, session = session).await()
            if (player == null) {
                LoginResponse.Full
            } else {
                LoginResponse.Success(player)
            }
        } catch (e: IllegalStateException) {
            logger.error(e) { "Error loading player $username" }
            LoginResponse.Failure
        }
    }


    override fun run() {
        var count = 0
        var next = queue.poll()
        while (next != null) {
            next.resume(Unit)
            if (count++ >= loginPerTickCap) {
                break
            }
            next = queue.poll()
        }
    }

}
