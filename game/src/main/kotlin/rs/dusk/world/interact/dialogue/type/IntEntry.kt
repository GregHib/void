package rs.dusk.world.interact.dialogue.type

import rs.dusk.engine.client.ui.dialogue.DialogueContext
import rs.dusk.network.codec.game.encode.sendScript

private const val INTEGER_ENTRY_SCRIPT = 108

suspend fun DialogueContext.intEntry(text: String): Int {
    player.sendScript(INTEGER_ENTRY_SCRIPT, text)
    return await("int")
}