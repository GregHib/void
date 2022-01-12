package world.gregs.voidps.engine.action

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.dsl.module
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.utility.get
import java.util.concurrent.LinkedBlockingQueue
import kotlin.coroutines.resume

/**
 * A scheduler for launching coroutines that aren't tied to a single action but can still require tick delays
 */
class Scheduler : Runnable, CoroutineScope {

    override val coroutineContext = Contexts.Game

    fun launch(block: suspend CoroutineScope.() -> Unit) = launch(context = Contexts.Game, block = block)

    private val queue = LinkedBlockingQueue<suspend (Long) -> Unit>()

    fun sync(block: suspend (Long) -> Unit) {
        queue.offer(block)
    }

    suspend fun await(): Long = suspendCancellableCoroutine { cont ->
        sync {
            cont.resume(it)
        }
    }

    suspend fun await(ticks: Int) {
        repeat(ticks) {
            await()
        }
    }

    override fun run() = runBlocking {
        while (queue.isNotEmpty()) {
            val next = queue.poll()
            try {
                next.invoke(GameLoop.tick)
            } catch (e: Throwable) {
                logger.warn(e) { "Error in game loop sync task" }
            }
        }
    }

    companion object {
        private val logger = InlineLogger()
    }
}

@Suppress("unused")
suspend fun CoroutineScope.delay(ticks: Int) {
    get<Scheduler>().await(ticks)
}

val schedulerModule = module {
    single(createdAtStart = true) { Scheduler() }
}