import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.ui.dialogue.dialogue
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.player.move
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.ObjectClick
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.world.Stairs

val stairs: Stairs by inject()

on<ObjectClick>({ it.action.type == ActionType.Climb }) { _: Player ->
    cancel()
}

on<ObjectOption> { player: Player ->
    climb(player, obj, option)
}

on<ObjectOption>({ option == "Climb" && (obj.def.options?.count { it?.startsWith("Climb") == true } ?: 0) > 1 }) { player: Player ->
    player.dialogue {
        val choice = choice(
            title = "What would you like to do?",
            text = """
                Go up the stairs.
                Go down the stairs.
                Never mind.
            """
        )
        when (choice) {
            1 -> climb(player, obj, "Climb-up")
            2 -> climb(player, obj, "Climb-down")
            else -> return@dialogue
        }
    }
}

fun climb(player: Player, obj: GameObject, option: String?) {
    val delta = stairs.get(obj.def.id, obj.tile, option ?: return) ?: return
    val name = obj.def.name.lowercase()
    player.action(ActionType.Climb) {
        if (name.contains("ladder") || name.contains("trapdoor")) {
            player.setAnimation(if (option == "Climb-down") "climb_down" else "climb_up")
            delay(2)
        }
        player.move(delta)
    }
}