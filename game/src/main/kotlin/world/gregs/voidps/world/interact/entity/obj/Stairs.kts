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

on<ObjectOption>({ operate && operate && option == "Climb" && hasClimbOption(def.options) }) { _: Player ->
    choice("What would you like to do?") {
        option("Go up the stairs.", block = { teleports.teleport(this, "Climb-up") })
        option("Go down the stairs.", block = { teleports.teleport(this, "Climb-down") })
        option("Never mind.")
    }
}

on<Teleport>({ takeoff && obj.name.isLadder() }) { player: Player ->
    val remaining = player.remaining("teleport_delay")
    if (remaining > 0) {
        delay = remaining
    } else if (remaining < 0) {
        player.setAnimation(if (option == "Climb-down") "climb_down" else "climb_up")
        player.start("teleport_delay", 2)
        delay = 2
    }
}

fun String.isLadder() = contains("ladder", true) || contains("rope", true) || contains("chain", true) || contains("vine", true) || isTrapDoor()

fun String.isTrapDoor(): Boolean {
    val name = replace(" ", "")
    return name.equals("trapdoor", true) || name.equals("manhole", true)
}

fun hasClimbOption(options: Array<String?>?): Boolean {
    val count = options?.count { it?.startsWith("Climb") == true } ?: return false
    return count > 1
}