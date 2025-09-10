package content.area.asgarnia.falador

import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.timer.timerStart
import world.gregs.voidps.engine.timer.timerStop
import world.gregs.voidps.engine.timer.timerTick
import world.gregs.voidps.engine.event.Script

import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.player.Player

internal suspend fun Interaction<Player>.openDressingRoom(id: String) {
    player.closeDialogue()
    delay(1)
    player.gfx("dressing_room_start")
    delay(1)
    player.open(id)
    player.softTimers.start("dressing_room")
}
@Script
class DressingRoom {

    init {
        timerStart("dressing_room") {
            interval = 1
        }

        timerTick("dressing_room") { player ->
            player.gfx("dressing_room")
        }

        timerStop("dressing_room") { player ->
            player.clearGfx()
            player["delay"] = 1
            player.closeMenu()
            player.gfx("dressing_room_finish")
            player.flagAppearance()
        }

    }

}
