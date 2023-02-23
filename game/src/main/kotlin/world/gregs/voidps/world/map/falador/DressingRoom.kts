import world.gregs.voidps.engine.client.ui.closeInterface
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.clearGraphic
import world.gregs.voidps.engine.entity.character.mode.interact.StopInteraction
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.clear
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.event.on

on<StopInteraction>({ it["dressing_room", false] }) { player: Player ->
    player.clearGraphic()
    player.start("delay", 1)
    player.closeInterface()
    player.setGraphic("dressing_room_finish")
    player.flagAppearance()
    player.clear("dressing_room")
}