package world.gregs.void.world.interact.dialogue.type

import world.gregs.void.engine.client.ui.dialogue.DialogueContext
import world.gregs.void.network.codec.game.encode.sendScript

private const val INTEGER_ENTRY_SCRIPT = 108

suspend fun DialogueContext.intEntry(text: String): Int {
    player.sendScript(INTEGER_ENTRY_SCRIPT, text)
    return await("int")
}