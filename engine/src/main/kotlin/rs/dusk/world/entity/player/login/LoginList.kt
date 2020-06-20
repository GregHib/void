package rs.dusk.world.entity.player.login

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.newSingleThreadContext
import org.koin.dsl.module
import rs.dusk.core.network.model.session.Session
import rs.dusk.engine.model.entity.factory.PlayerFactory
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque
import kotlin.coroutines.Continuation
import kotlin.coroutines.suspendCoroutine

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 31, 2020
 */
val loginQueueModule = module {
    single { LoginList(get()) }
}

class LoginList(
    private val factory: PlayerFactory,
    val queue: ConcurrentLinkedDeque<Continuation<Unit>> = ConcurrentLinkedDeque()
) : Deque<Continuation<Unit>> by queue {

    private val logger = InlineLogger()

    fun add(username: String, session: Session? = null) = scope.async {
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

    companion object {
        private val scope = CoroutineScope(newSingleThreadContext("LoginQueue"))
    }
}