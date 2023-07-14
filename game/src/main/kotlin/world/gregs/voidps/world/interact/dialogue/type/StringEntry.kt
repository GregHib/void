package world.gregs.voidps.world.interact.dialogue.type

import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.suspend.dialogue.StringSuspension

private const val STRING_ENTRY_SCRIPT = 109

suspend fun CharacterContext.stringEntry(text: String): String {
    player.sendScript(STRING_ENTRY_SCRIPT, text)
    return StringSuspension()
}