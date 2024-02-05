package world.gregs.voidps.world.interact.entity.effect

import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.timer.characterTimerStart
import world.gregs.voidps.engine.timer.characterTimerStop
import world.gregs.voidps.engine.timer.characterTimerTick
import kotlin.math.sign

characterTimerStart({ timer == "movement_delay" }) { character: Character ->
    character.start("movement_delay", -1)
    interval = 1
}

characterTimerTick({ timer == "movement_delay" }) { character: Character ->
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

characterTimerStop({ timer == "movement_delay" }) { character: Character ->
    character.stop("movement_delay")
}