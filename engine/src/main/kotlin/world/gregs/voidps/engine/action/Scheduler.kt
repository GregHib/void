package world.gregs.voidps.engine.action

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.dsl.module
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.delay
import kotlin.coroutines.resume

/**
 * A scheduler for launching coroutines that aren't tied to a single action but can still require tick delays
 */
class Scheduler : CoroutineScope {

    override val coroutineContext = Contexts.Game

    fun launch(block: suspend CoroutineScope.() -> Unit) = launch(context = Contexts.Game, block = block)


    private val list = mutableListOf<suspend (Long) -> Unit>()

    fun sync(block: suspend (Long) -> Unit) {
        list.add(block)
    }

    suspend fun tick() {
        val it = list.iterator()
        while (it.hasNext()) {
            val next = it.next()
            next.invoke(GameLoop.tick)
            it.remove()
        }
    }

}

@Suppress("unused")
suspend fun CoroutineScope.delay(ticks: Int) {
    suspendCancellableCoroutine<Unit> { continuation ->
        delay(ticks) {
            continuation.resume(Unit)
        }
    }
}

val schedulerModule = module {
    single(createdAtStart = true) { Scheduler() }
}