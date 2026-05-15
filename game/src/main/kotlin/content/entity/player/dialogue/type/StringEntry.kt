package content.entity.player.dialogue.type

import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.suspend.pauseString

suspend fun Player.stringEntry(text: String, placeholder: String? = null): String {
    sendScript("string_entry", text)
    if (placeholder != null) {
        sendScript("set_entry_string", placeholder)
    }
    return pauseString()
}
