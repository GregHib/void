package world.gregs.voidps.world.map.falador

import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.clearGraphic
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.timer.timerStart
import world.gregs.voidps.engine.timer.timerStop
import world.gregs.voidps.engine.timer.timerTick

timerStart({ timer == "dressing_room" }) { _: Player ->
    interval = 1
}

timerTick({ timer == "dressing_room" }) { player: Player ->
    player.setGraphic("dressing_room")
}

timerStop({ timer == "dressing_room" }) { player: Player ->
    player.clearGraphic()
    player.start("delay", 1)
    player.closeMenu()
    player.setGraphic("dressing_room_finish")
    player.flagAppearance()
}