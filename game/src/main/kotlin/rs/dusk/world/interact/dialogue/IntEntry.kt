package rs.dusk.world.interact.dialogue

import rs.dusk.engine.client.send
import rs.dusk.engine.client.ui.dialogue.Dialogues
import rs.dusk.network.rs.codec.game.encode.message.ScriptMessage

private const val INTEGER_ENTRY_SCRIPT = 109

suspend fun Dialogues.intEntry(text: String): Int {
    player.send(ScriptMessage(INTEGER_ENTRY_SCRIPT, text))
    return await("int")
}