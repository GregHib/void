package world.gregs.voidps.world.interact.dialogue.type

import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.event.CharacterContext
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.suspend.dialogue.StringSuspension

suspend fun CharacterContext<Player>.stringEntry(text: String): String {
    player.sendScript("string_entry", text)
    return StringSuspension()
}