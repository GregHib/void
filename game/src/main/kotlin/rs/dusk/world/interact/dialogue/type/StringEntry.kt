package rs.dusk.world.interact.dialogue.type

import rs.dusk.engine.client.send
import rs.dusk.engine.client.ui.dialogue.DialogueContext
import rs.dusk.network.rs.codec.game.encode.message.ScriptMessage

private const val STRING_ENTRY_SCRIPT = 109

suspend fun DialogueContext.stringEntry(text: String): String {
    player.send(ScriptMessage(STRING_ENTRY_SCRIPT, text))
    return await("string")
}