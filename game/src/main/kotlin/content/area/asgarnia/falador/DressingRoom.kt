package content.area.asgarnia.falador

import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.timer.*

internal suspend fun Interaction<Player>.openDressingRoom(id: String) {
    player.closeDialogue()
    delay(1)
    player.gfx("dressing_room_start")
    delay(1)
    player.open(id)
    player.softTimers.start("dressing_room")
}

@Script
class DressingRoom : Api {

    @Timer("dressing_room")
    override fun start(player: Player, timer: String, restart: Boolean): Int = 1

    @Timer("dressing_room")
    override fun tick(player: Player, timer: String): Int {
        player.gfx("dressing_room")
        return Timer.CONTINUE
    }

    @Timer("dressing_room")
    override fun stop(player: Player, timer: String, logout: Boolean) {
        player.clearGfx()
        player["delay"] = 1
        player.closeMenu()
        player.gfx("dressing_room_finish")
        player.flagAppearance()
    }
}
