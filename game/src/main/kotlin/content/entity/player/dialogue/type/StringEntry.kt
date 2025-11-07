package content.entity.player.dialogue.type

import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.suspend.StringSuspension

suspend fun Player.stringEntry(text: String): String {
    sendScript("string_entry", text)
    return StringSuspension.get(this)
}
