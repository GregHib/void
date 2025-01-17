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

fun Player.resumeDialogueSuspension(): Boolean {
    val suspend = dialogueSuspension ?: return false
    if (suspend.ready()) {
        dialogueSuspension = null
        suspend.resume()
    }
    return true
}

/**
 * Prevents non-interface player input and most processing
 */
suspend fun Context<*>.delay(ticks: Int = 1) {
    if (ticks <= 0) {
        return
    }
    character.start("delay", ticks)
    suspendCancellableCoroutine {
        character.delay = it
    }
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

/**
 * Movement delay, typically used by interactions that perform animations or exact movements
 */
suspend fun Context<*>.arriveDelay() {
    val delay = character.remaining("last_movement")
    if (delay == -1) {
        return
    }
    delay(delay)
}

context(Context<*>) fun Character.approachRange(range: Int?, update: Boolean = true) {
    val interact = mode as? Interact ?: return
    interact.updateRange(range, update)
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

context(Action<*>) suspend fun Character.playAnimation(id: String, override: Boolean = false, canInterrupt: Boolean = true) {
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