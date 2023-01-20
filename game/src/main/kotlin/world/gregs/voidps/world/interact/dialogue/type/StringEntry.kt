package world.gregs.voidps.world.interact.dialogue.type

import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.dialogue.DialogueContext
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.event.suspend.StringSuspension

private const val STRING_ENTRY_SCRIPT = 109

suspend fun DialogueContext.stringEntry(text: String): String {
    player.sendScript(STRING_ENTRY_SCRIPT, text)
    return await("string")
}

context(Interaction) suspend fun stringEntry(text: String): String {
    player.sendScript(STRING_ENTRY_SCRIPT, text)
    return StringSuspension()
}