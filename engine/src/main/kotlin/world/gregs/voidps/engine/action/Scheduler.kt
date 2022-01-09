package world.gregs.voidps.engine.action

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.dsl.module
import world.gregs.voidps.engine.delay
import kotlin.coroutines.resume

/**
 * A scheduler for launching coroutines that aren't tied to a single action but can still require tick delays
 */
class Scheduler : CoroutineScope {

    override val coroutineContext = Contexts.Game

    fun launch(block: suspend CoroutineScope.() -> Unit) = launch(context = Contexts.Game, block = block)
    
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