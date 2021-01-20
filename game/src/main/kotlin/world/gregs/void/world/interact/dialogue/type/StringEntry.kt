package world.gregs.void.world.interact.dialogue.type

import world.gregs.void.engine.client.ui.dialogue.DialogueContext
import world.gregs.void.network.codec.game.encode.sendScript

private const val STRING_ENTRY_SCRIPT = 109

suspend fun DialogueContext.stringEntry(text: String): String {
    player.sendScript(STRING_ENTRY_SCRIPT, text)
    return await("string")
}