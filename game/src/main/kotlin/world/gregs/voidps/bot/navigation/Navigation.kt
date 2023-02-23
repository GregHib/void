package world.gregs.voidps.bot.navigation

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import world.gregs.voidps.bot.Bot
import world.gregs.voidps.engine.client.variable.removeVar
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.timer.TICKS
import kotlin.coroutines.resume
import kotlin.reflect.KClass

suspend fun Bot.await(type: Any, timeout: Int = -1) {
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
    noinline condition: E.(T) -> Boolean = { true }
) {
    suspendCancellableCoroutine<Unit> { cont ->
        player.getOrPut("bot_suspensions") { mutableMapOf<KClass<E>, Pair<E.(T) -> Boolean, CancellableContinuation<Unit>>>() }[E::class] = condition to cont
    }
}

fun Bot.resume(type: Any) = resume(type, Unit)

fun <T : Any> Bot.resume(type: Any, value: T) {
    if (player.contains("suspension") && player.get<Any>("suspension") == type) {
        val cont: CancellableContinuation<T>? = player.removeVar("cont")
        cont?.resume(value)
    }
}

fun Bot.cancel(cause: Throwable? = null) {
    val cont: CancellableContinuation<*>? = player.removeVar("cont")
    cont?.cancel(cause)
}