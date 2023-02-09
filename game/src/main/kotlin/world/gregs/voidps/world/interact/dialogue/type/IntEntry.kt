package world.gregs.voidps.world.interact.dialogue.type

import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.entity.character.player.PlayerContext
import world.gregs.voidps.engine.suspend.IntSuspension

private const val INTEGER_ENTRY_SCRIPT = 108

suspend fun PlayerContext.intEntry(text: String): Int {
    player.sendScript(INTEGER_ENTRY_SCRIPT, text)
    return IntSuspension(player)
}