package world.gregs.voidps.world.interact.dialogue.type

import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.suspend.dialogue.IntSuspension

private const val INTEGER_ENTRY_SCRIPT = 108

suspend fun CharacterContext.intEntry(text: String): Int {
    player.sendScript(INTEGER_ENTRY_SCRIPT, text)
    return IntSuspension()
}