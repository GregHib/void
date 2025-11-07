package world.gregs.voidps.engine.suspend

import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player

fun Character.resumeSuspension(): Boolean {
    val suspend = suspension ?: return false
    if (suspend.ready()) {
        suspension = null
        suspend.resume()
    }
    return true
}

suspend fun Player.awaitDialogues(): Boolean {
    Suspension.start(this) { dialogue == null }
    return true
}
