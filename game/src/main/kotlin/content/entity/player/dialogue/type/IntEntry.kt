package content.entity.player.dialogue.type

import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.suspend.IntSuspension

suspend fun Player.intEntry(text: String): Int {
    sendScript("int_entry", text)
    return IntSuspension.get(this)
}
