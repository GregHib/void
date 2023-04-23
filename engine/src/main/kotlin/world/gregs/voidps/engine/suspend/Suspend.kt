package world.gregs.voidps.engine.suspend

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerContext
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

suspend fun CharacterContext.pause(ticks: Int = 1) {
    TickSuspension(ticks)
}

suspend fun PlayerContext.awaitDialogues(): Boolean {
    PredicateSuspension { player.dialogue == null }
    return true
}

suspend fun PlayerContext.awaitInterfaces(): Boolean {
    PredicateSuspension { player.menu == null }
    return true
}

suspend fun PlayerContext.pauseForever() {
    InfiniteSuspension()
}

suspend fun PlayerContext.arriveDelay() {
    val delay = player.remaining("last_movement")
    if (delay == -1) {
        return
    }
    pause(delay)
}

context(PlayerContext) fun Player.approachRange(range: Int?, update: Boolean = true) {
    val interact = mode as? Interact ?: return
    interact.updateRange(range, update)
}

private val logger = InlineLogger()

context(PlayerContext) suspend fun Player.playAnimation(id: String, override: Boolean = false) {
    val ticks = setAnimation(id, override = override)
    if (ticks == -1) {
        logger.warn { "No animation delay $id" }
    } else {
        player.start("movement_delay", ticks)
        pause(ticks)
    }
}