package content.area.asgarnia.falador

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.timer.*

internal suspend fun Player.openDressingRoom(id: String) {
    closeDialogue()
    delay(1)
    gfx("dressing_room_start")
    delay(1)
    open(id)
    softTimers.start("dressing_room")
}

class DressingRoom : Script {
    init {
        timerStart("dressing_room") { 1 }

        timerTick("dressing_room") {
            gfx("dressing_room")
            Timer.CONTINUE
        }

        timerStop("dressing_room") {
            clearGfx()
            this["delay"] = 1
            closeMenu()
            gfx("dressing_room_finish")
            flagAppearance()
        }
    }
}
