import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.ui.dialogue.dialogue
import world.gregs.voidps.engine.entity.character.mode.interact.onOperate
import world.gregs.voidps.engine.entity.character.mode.interact.option.def
import world.gregs.voidps.engine.entity.character.mode.interact.option.option
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.ObjectClick
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.world.Stairs

val stairs: Stairs by inject()

on<ObjectClick>({ it.action.type == ActionType.Climb }) { _: Player ->
    cancel()
}

onOperate { player: Player, obj: GameObject ->
    climb(player, obj, obj.def, option)
}

onOperate({ option == "Climb" && (def.options?.count { it?.startsWith("Climb") == true } ?: 0) > 1 }) { player: Player, obj: GameObject ->
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
            1 -> climb(player, obj, def, "Climb-up")
            2 -> climb(player, obj, def, "Climb-down")
            else -> return@dialogue
        }
    }
}

fun climb(player: Player, obj: GameObject, def: ObjectDefinition, option: String?) {
    val teleport = stairs.get(def.id, obj.tile, option ?: return) ?: return
    val name = def.name.lowercase()
    player.action(ActionType.Climb) {
        if (name.contains("ladder") || name.contains("trapdoor")) {
            player.setAnimation(if (option == "Climb-down") "climb_down" else "climb_up")
            delay(2)
        }
        teleport.apply(player)
    }
}