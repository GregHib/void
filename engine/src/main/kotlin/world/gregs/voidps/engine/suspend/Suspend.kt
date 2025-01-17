package world.gregs.voidps.engine.suspend

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.event.Context
import world.gregs.voidps.engine.queue.Action

fun Character.resumeSuspension(): Boolean {
    val suspend = suspension ?: return false
    if (suspend.ready()) {
        suspension = null
        suspend.resume()
    }
    return true
}

suspend fun SuspendableContext<Player>.awaitDialogues(): Boolean {
    PredicateSuspension { player.dialogue == null }
    return true
}

suspend fun SuspendableContext<Player>.awaitInterfaces(): Boolean {
    PredicateSuspension { player.menu == null }
    return true
}

suspend fun SuspendableContext<*>.pauseForever() {
    InfiniteSuspension()
}

private val logger = InlineLogger()

context(SuspendableContext<*>) suspend fun Character.playAnimation(id: String, override: Boolean = false, canInterrupt: Boolean = true) {
    val ticks = setAnimation(id, override = override)
    if (ticks == -1) {
        logger.warn { "No animation delay $id" }
    } else {
        character.start("movement_delay", ticks)
        if (canInterrupt) {
            pause(ticks)
        } else {
            delay(ticks)
        }
    }
}