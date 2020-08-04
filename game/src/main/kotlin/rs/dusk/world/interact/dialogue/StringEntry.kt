package rs.dusk.world.interact.dialogue

import rs.dusk.engine.client.send
import rs.dusk.engine.client.ui.dialogue.Dialogues
import rs.dusk.network.rs.codec.game.encode.message.ScriptMessage

private const val STRING_ENTRY_SCRIPT = 109

suspend fun Dialogues.stringEntry(text: String): String {
    player.send(ScriptMessage(STRING_ENTRY_SCRIPT, text))
    return await("string")
}