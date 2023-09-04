package world.gregs.voidps.world.interact.entity.obj

import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.dialogue.type.choice

val teleports: Teleports by inject()

on<ObjectOption>({ operate && operate && option == "Climb" && (def.options?.count { it?.startsWith("Climb") == true } ?: 0) > 1 }) { _: Player ->
    choice("What would you like to do?") {
        option("Go up the stairs.", block = { teleports.teleport(this, "Climb-up") })
        option("Go down the stairs.", block = { teleports.teleport(this, "Climb-down") })
        option("Never mind.")
    }
}

on<Teleport>({ obj.name.lowercase().isLadder() }) { player: Player ->
    val remaining = player.remaining("teleport_delay")
    if (remaining > 0) {
        delay = remaining
    } else if (remaining < 0) {
        player.setAnimation(if (option == "Climb-down") "climb_down" else "climb_up")
        player.start("teleport_delay", 2)
        delay = 2
    }
}

fun String.isLadder() = contains("ladder") || contains("rope") || contains("chain") || contains("vine") || isTrapDoor()

fun String.isTrapDoor(): Boolean {
    val name = replace(" ", "").lowercase()
    return name == "trapdoor" || name == "manhole"
}