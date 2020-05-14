package rs.dusk.engine.client

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.koin.dsl.module
import rs.dusk.core.network.model.session.Session
import rs.dusk.engine.entity.factory.PlayerFactory
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque
import kotlin.coroutines.Continuation
import kotlin.coroutines.suspendCoroutine

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 31, 2020
 */
val loginQueueModule = module {
    single { LoginQueue(get()) }
}

class LoginQueue(
    private val factory: PlayerFactory,
    val queue: ConcurrentLinkedDeque<Continuation<Unit>> = ConcurrentLinkedDeque<Continuation<Unit>>()
) : Deque<Continuation<Unit>> by queue {

    private val logger = InlineLogger()

    fun add(username: String, session: Session? = null) = GlobalScope.async {
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

}