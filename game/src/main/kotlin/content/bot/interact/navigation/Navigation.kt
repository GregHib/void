package content.bot.interact.navigation

import content.bot.Bot
import content.bot.bot
import content.bot.isBot
import content.entity.obj.door.Door
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.mode.move.Movement
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.timer.TICKS
import kotlin.collections.set
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

suspend fun Bot.awaitInteract(timeout: Int = -1) {
    await("tick", timeout)
    while (player.mode is Interact || player.hasClock("movement_delay")) {
        await("tick", timeout)
    }
}

suspend inline fun Bot.await(
    noinline condition: Player.() -> Boolean = { true },
) {
    suspendCancellableCoroutine { cont ->
        player.getOrPut("bot_new_suspensions") { mutableListOf<Pair<Player.() -> Boolean, CancellableContinuation<Unit>>>() }.add(condition to cont)
    }
}

fun Bot.resume(type: Any) = resume(type, Unit)

fun <T : Any> Bot.resume(type: Any, value: T) {
    if (player.get<Any>("suspension") == type) {
        val cont: CancellableContinuation<T>? = player.remove("cont")
        cont?.resume(value)
    }
}

fun Bot.cancel(cause: Throwable? = null) {
    val cont: CancellableContinuation<*>? = player.remove("cont")
    cont?.cancel(cause)
}

class Navigation : Script {

    init {
        moved {
            if (isBot && ((mode is Movement && steps.size <= 1) || mode == EmptyMode)) {
                bot.resume("move")
            }
        }

        Door.opened = {
            if (isBot) {
                bot.resume("move")
            }
        }

        objTeleportLand { _, _ ->
            if (isBot) {
                bot.resume("move")
            }
        }
    }
}
