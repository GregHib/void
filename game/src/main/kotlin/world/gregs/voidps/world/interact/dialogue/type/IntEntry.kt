package world.gregs.voidps.world.interact.dialogue.type

import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.suspend.dialogue.IntSuspension

suspend fun CharacterContext.intEntry(text: String): Int {
    player.sendScript("int_entry", text)
    return IntSuspension()
}