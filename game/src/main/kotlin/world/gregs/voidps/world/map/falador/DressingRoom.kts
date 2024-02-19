package world.gregs.voidps.world.map.falador

import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.clearGraphic
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.timer.timerStart
import world.gregs.voidps.engine.timer.timerStop
import world.gregs.voidps.engine.timer.timerTick

timerStart("dressing_room") { _ ->
    interval = 1
}

timerTick("dressing_room") { player ->
    player.setGraphic("dressing_room")
}

timerStop("dressing_room") { player ->
    player.clearGraphic()
    player.start("delay", 1)
    player.closeMenu()
    player.setGraphic("dressing_room_finish")
    player.flagAppearance()
}