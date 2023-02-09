package world.gregs.voidps.world.interact.dialogue.type

import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.entity.character.player.PlayerContext
import world.gregs.voidps.engine.suspend.StringSuspension

private const val STRING_ENTRY_SCRIPT = 109

suspend fun PlayerContext.stringEntry(text: String): String {
    player.sendScript(STRING_ENTRY_SCRIPT, text)
    return StringSuspension(player)
}