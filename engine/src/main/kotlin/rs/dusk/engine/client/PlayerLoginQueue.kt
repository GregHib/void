package rs.dusk.engine.client

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.koin.dsl.module
import rs.dusk.core.network.model.session.Session
import rs.dusk.engine.EngineTasks
import rs.dusk.engine.entity.factory.PlayerFactory
import rs.dusk.utility.inject
import java.util.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 31, 2020
 */
@Suppress("USELESS_CAST")
val clientLoginQueueModule = module {
    single { PlayerLoginQueue(get()) as LoginQueue }
}

class PlayerLoginQueue(tasks: EngineTasks) : LoginQueue(tasks, 10) {

    private val factory: PlayerFactory by inject()
    private val logger = InlineLogger()
    val queue = LinkedList<Continuation<Unit>>()

    override fun add(username: String, session: Session?) = GlobalScope.async {
        suspendCoroutine<Unit> {
            queue.add(it)
        }

        try {
            val player = factory.spawn(username, session).await()
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
        var next = queue.poll()
        while (next != null) {
            next.resume(Unit)
            next = queue.poll()
        }
    }
}
