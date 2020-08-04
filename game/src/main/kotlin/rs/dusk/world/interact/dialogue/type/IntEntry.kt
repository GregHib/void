package rs.dusk.world.interact.dialogue.type

import rs.dusk.engine.client.send
import rs.dusk.engine.client.ui.dialogue.DialogueContext
import rs.dusk.network.rs.codec.game.encode.message.ScriptMessage

private const val INTEGER_ENTRY_SCRIPT = 108

suspend fun DialogueContext.intEntry(text: String): Int {
    player.send(ScriptMessage(INTEGER_ENTRY_SCRIPT, text))
    return await("int")
}