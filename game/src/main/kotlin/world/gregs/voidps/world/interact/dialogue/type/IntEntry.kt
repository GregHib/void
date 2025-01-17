package world.gregs.voidps.world.interact.dialogue.type

import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.event.Context
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.suspend.dialogue.IntSuspension

suspend fun Context<Player>.intEntry(text: String): Int {
    player.sendScript("int_entry", text)
    return IntSuspension.get(player)
}