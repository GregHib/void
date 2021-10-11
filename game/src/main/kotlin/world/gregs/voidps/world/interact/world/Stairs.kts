import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.player.move
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.obj.ObjectClick
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.world.interact.world.Stairs

val stairs: Stairs by inject()

on<ObjectClick>({ it.action.type == ActionType.Climb }) { player: Player ->
    cancel = true
}

on<ObjectOption> { player: Player ->
    val delta = stairs.get(obj.id, obj.tile, option ?: return@on) ?: return@on
    val name = obj.def.name.toLowerCase()
    player.action(ActionType.Climb) {
        if (name.contains("ladder") || name.contains("trapdoor")) {
            player.setAnimation(if (option == "Climb-down") "climb_down" else "climb_up")
            delay(2)
        }
        player.move(delta)
        delay(1)
    }
}