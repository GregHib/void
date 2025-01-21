package world.gregs.voidps.engine.suspend

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.Character
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

suspend fun SuspendableContext<Player>.awaitDialogues(): Boolean {
    Suspension.start(character) { player.dialogue == null }
    return true
}

suspend fun SuspendableContext<Player>.awaitInterfaces(): Boolean {
    Suspension.start(character) { player.menu == null }
    return true
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