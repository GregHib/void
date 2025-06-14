package content.entity.player.dialogue.type

import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Context
import world.gregs.voidps.engine.suspend.StringSuspension

suspend fun Context<Player>.warning(id: String): Boolean {
    val count = player["warning_$id", 0]
    if (count == 7) {
        return true
    }
    check(player.open("warning_$id")) { "Unable to open warning dialogue warning_$id for $player" }
    player.interfaces.sendVisibility("warning_$id", "ask_again", count == 6)
    val result = StringSuspension.get(player) == "yes"
    player.close("warning_$id")
    return result
}
