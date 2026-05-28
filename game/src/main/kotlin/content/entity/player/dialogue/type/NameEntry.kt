package content.entity.player.dialogue.type

import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.suspend.Suspension.NameEntry

suspend fun Player.nameEntry(text: String): String {
    sendScript("name_entry", text)
    val name: String = suspendCancellableCoroutine {
        suspension = NameEntry(it)
    }
    suspension = null
    return name
}
