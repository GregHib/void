package world.gregs.voidps.world.interact.world

import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.suspend.arriveDelay
import world.gregs.voidps.engine.suspend.pause
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.world.spawn.Stairs

val stairs: Stairs by inject()

on<ObjectOption>({ operate && stairs.get(def.id, obj.tile, option) != null }) { _: Player ->
    arriveDelay()
    climb(option)
}

on<ObjectOption>({ operate && operate && option == "Climb" && (def.options?.count { it?.startsWith("Climb") == true } ?: 0) > 1 }) { _: Player ->
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
        val remaining = player.remaining("climb_delay")
        if (remaining > 0) {
            pause(remaining)
        } else if (remaining < 0) {
            player.setAnimation(if (option == "Climb-down") "climb_down" else "climb_up")
            player.start("climb_delay", 2)
            pause(2)
        }
    } else {
        player.start("climb_delay", 1)
    }
    val tile = teleport.apply(player.tile)
    player.tele(tile)
    player.events.emit(Climb)
}