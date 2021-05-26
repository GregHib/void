package world.gregs.voidps.bot.navigation

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.engine.entity.character.player.Bot
import world.gregs.voidps.engine.entity.contains
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.remove
import world.gregs.voidps.engine.entity.set
import kotlin.coroutines.resume

suspend fun Bot.await(type: Any) = suspendCancellableCoroutine<Unit> { cont ->
    player["suspension"] = type
    player["cont"] = cont
}

fun Bot.resume(type: Any) = resume(type, Unit)

fun <T : Any> Bot.resume(type: Any, value: T) {
    if (player.contains("suspension") && player.get<Any>("suspension") == type) {
        val cont: CancellableContinuation<T>? = player.remove("cont")
        cont?.resume(value)
    }
}