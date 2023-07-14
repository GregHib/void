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
import world.gregs.voidps.engine.suspend.delay
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.world.spawn.Stairs

val stairs: Stairs by inject()

on<ObjectOption>({ operate && stairs.get(def.id, target.tile, option) != null }) { _: Player ->
    arriveDelay()
    climb(option)
}

on<ObjectOption>({ operate && operate && option == "Climb" && (def.options?.count { it?.startsWith("Climb") == true } ?: 0) > 1 }) { _: Player ->
    choice("What would you like to do?") {
        option("Go up the stairs.", block = { climb("Climb-up") })
        option("Go down the stairs.", block = { climb("Climb-down") })
        option("Never mind.")
    }
}

suspend fun ObjectOption.climb(option: String) {
    val teleport = stairs.get(def.id, target.tile, option) ?: return
    val name = def.name.lowercase()
    if (name.isLadder()) {
        val remaining = player.remaining("climb_delay")
        if (remaining > 0) {
            delay(remaining)
        } else if (remaining < 0) {
            player.setAnimation(if (option == "Climb-down") "climb_down" else "climb_up")
            player.start("climb_delay", 2)
            delay(2)
        }
    } else {
        player.start("climb_delay", 1)
    }
    val tile = teleport.apply(player.tile)
    player.tele(tile)
    player.events.emit(Climb)
}

fun String.isLadder() = contains("ladder") || contains("rope") || contains("chain") || contains("vine") || isTrapDoor()

fun String.isTrapDoor(): Boolean {
    val name = replace(" ", "").lowercase()
    return name == "trapdoor" || name == "manhole"
}