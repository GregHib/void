package world.gregs.voidps.world.interact.world

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.suspend.pause
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.world.spawn.Stairs

val stairs: Stairs by inject()

on<ObjectOption>({ stairs.get(def.id, obj.tile, option) != null }) { _: Player ->
    climb(option)
}

on<ObjectOption>({ option == "Climb" && (def.options?.count { it?.startsWith("Climb") == true } ?: 0) > 1 }) { _: Player ->
    val choice = choice(
        title = "What would you like to do?",
        text = """
            Go up the stairs.
            Go down the stairs.
            Never mind.
        """
    )
    when (choice) {
        1 -> climb("Climb-up")
        2 -> climb("Climb-down")
        else -> return@on
    }
}

suspend fun ObjectOption.climb(option: String) {
    val teleport = stairs.get(def.id, obj.tile, option) ?: return
    val name = def.name.lowercase()
    if (name.contains("ladder") || name.contains("trapdoor")) {
        player.setAnimation(if (option == "Climb-down") "climb_down" else "climb_up")
        pause(2)
    }
    teleport.apply(player)
    player.events.emit(Climb)
}