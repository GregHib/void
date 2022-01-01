package world.gregs.voidps.bot.navigation

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.player.Bot
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventHandler
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.utility.TICKS
import kotlin.coroutines.resume

suspend fun Bot.await(type: Any, timeout: Long = -1) {
    if (timeout > 0) {
        withTimeoutOrNull(TICKS.toMillis(timeout)) {
            suspendCancellableCoroutine<Unit> { cont ->
                player["suspension"] = type
                player["cont"] = cont
            }
        }
    } else {
        suspendCancellableCoroutine<Unit> { cont ->
            player["suspension"] = type
            player["cont"] = cont
        }
    }
}

suspend inline fun <reified T : Entity, reified E : Event> Bot.await(
    noinline condition: E.(T) -> Boolean = { true },
    priority: Priority = Priority.MEDIUM
) {
    var handler: EventHandler? = null
    suspendCancellableCoroutine<Unit> { cont ->
        handler = player.events.on(condition, priority) {
            cont.resume(Unit)
        }
    }
    handler?.let {
        player.events.remove(it)
    }
}

fun Bot.resume(type: Any) = resume(type, Unit)

fun <T : Any> Bot.resume(type: Any, value: T) {
    if (player.contains("suspension") && player.get<Any>("suspension") == type) {
        val cont: CancellableContinuation<T>? = player.remove("cont")
        cont?.resume(value)
    }
}

fun Bot.cancel(cause: Throwable? = null) {
    val cont: CancellableContinuation<*>? = player.remove("cont")
    cont?.cancel(cause)
}