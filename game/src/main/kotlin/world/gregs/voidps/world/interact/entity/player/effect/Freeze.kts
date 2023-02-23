package world.gregs.voidps.world.interact.entity.player.effect

import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.TimerStart
import world.gregs.voidps.engine.timer.TimerStop
import world.gregs.voidps.engine.timer.TimerTick
import kotlin.math.sign

on<TimerStart>({ timer == "movement_delay" }) { character: Character ->
    character.start("movement_delay", -1)
    interval = 1
}

on<TimerTick>({ timer == "movement_delay" }) { character: Character ->
    val frozen = character.frozen
    character.movementDelay -= character.movementDelay.sign
    if (character.movementDelay == 0) {
        if (frozen) {
            character.movementDelay = -5
        } else {
            cancel()
        }
    }
}

on<TimerStop>({ timer == "movement_delay" }) { character: Character ->
    character.stop("movement_delay")
}