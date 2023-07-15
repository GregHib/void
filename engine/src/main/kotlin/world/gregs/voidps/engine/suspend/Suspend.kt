package world.gregs.voidps.engine.suspend

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation

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
suspend fun CharacterContext.delay(ticks: Int = 1) {
    if (ticks <= 0) {
        return
    }
    character.start("delay", ticks)
    suspendCancellableCoroutine {
        character.delay = it
    }
}

/**
 * Interrupt-able pausing
 */
suspend fun CharacterContext.pause(ticks: Int = 1) {
    TickSuspension(ticks)
}

suspend fun CharacterContext.awaitDialogues(): Boolean {
    if (character !is Player) {
        return false
    }
    PredicateSuspension { player.dialogue == null }
    return true
}

suspend fun CharacterContext.awaitInterfaces(): Boolean {
    if (character !is Player) {
        return false
    }
    PredicateSuspension { player.menu == null }
    return true
}

suspend fun CharacterContext.pauseForever() {
    InfiniteSuspension()
}

/**
 * Movement delay, typically used by interactions that perform animations or force movements
 */
suspend fun CharacterContext.arriveDelay() {
    val delay = character.remaining("last_movement")
    if (delay == -1) {
        return
    }
    delay(delay)
}

context(CharacterContext) fun Character.approachRange(range: Int?, update: Boolean = true) {
    val interact = mode as? Interact ?: return
    interact.updateRange(range, update)
}

private val logger = InlineLogger()

context(CharacterContext) suspend fun Character.playAnimation(id: String, override: Boolean = false) {
    val ticks = setAnimation(id, override = override)
    if (ticks == -1) {
        logger.warn { "No animation delay $id" }
    } else {
        character.start("movement_delay", ticks)
        pause(ticks)
    }
}