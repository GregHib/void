package world.gregs.voidps.world.interact.dialogue.type

import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.entity.character.player.PlayerContext
import world.gregs.voidps.engine.event.suspend.IntSuspension

private const val INTEGER_ENTRY_SCRIPT = 108

context(PlayerContext) suspend fun intEntry(text: String): Int {
    player.sendScript(INTEGER_ENTRY_SCRIPT, text)
    return IntSuspension(player)
}