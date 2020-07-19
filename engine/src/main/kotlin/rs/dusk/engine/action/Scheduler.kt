package rs.dusk.engine.action

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.dsl.module
import rs.dusk.engine.event.Priority.SCHEDULE_PROCESS
import rs.dusk.engine.event.priority
import rs.dusk.engine.event.then
import rs.dusk.engine.model.engine.Tick
import rs.dusk.utility.get
import kotlin.coroutines.resume

/**
 * A scheduler for launching coroutines that aren't tied to a single action but can still require tick delays
 */
class Scheduler : CoroutineScope {

    override val coroutineContext = Contexts.Engine

    val active = mutableListOf<CancellableContinuation<Unit>>()

    fun add(block: suspend CoroutineScope.() -> Unit) = launch(context = Contexts.Engine, block = block)

    init {
        Tick priority SCHEDULE_PROCESS then {
            val iterator = active.iterator()
            while (iterator.hasNext()) {
                val next = iterator.next()
                next.resume(Unit)
                if (!next.isActive || next.isCancelled || next.isCompleted) {
                    iterator.remove()
                }
            }
        }
    }

    suspend fun delay(ticks: Int) {
        repeat(ticks) {
            suspendCancellableCoroutine<Unit> {
                active.add(it)
            }
        }
    }
}

@Suppress("unused")
suspend fun CoroutineScope.delay(ticks: Int) {
    get<Scheduler>().delay(ticks)
}

val schedulerModule = module {
    single(createdAtStart = true) { Scheduler() }
}