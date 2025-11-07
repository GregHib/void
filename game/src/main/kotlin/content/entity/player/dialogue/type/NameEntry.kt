package content.entity.player.dialogue.type

import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.suspend.NameSuspension

suspend fun Player.nameEntry(text: String): String {
    sendScript("name_entry", text)
    return NameSuspension.get(this)
}
