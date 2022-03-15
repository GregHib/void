import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.entity.character.event.CantReach
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.cantReach
import world.gregs.voidps.engine.event.on

on<CantReach>({ it.action.type != ActionType.OpenDoor }) { player: Player ->
    player.cantReach()
}