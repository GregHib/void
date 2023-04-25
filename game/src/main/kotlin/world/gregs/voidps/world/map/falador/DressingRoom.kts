import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.clearGraphic
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.TimerStart
import world.gregs.voidps.engine.timer.TimerStop
import world.gregs.voidps.engine.timer.TimerTick

on<TimerStart>({ timer == "dressing_room" }) { _: Player ->
    interval = 1
}

on<TimerTick>({ timer == "dressing_room" }) { player: Player ->
    player.setGraphic("dressing_room")
}

on<TimerStop>({ timer == "dressing_room" }) { player: Player ->
    player.clearGraphic()
    player.start("delay", 1)
    player.closeMenu()
    player.setGraphic("dressing_room_finish")
    player.flagAppearance()
}