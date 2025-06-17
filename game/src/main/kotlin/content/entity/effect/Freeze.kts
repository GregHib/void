package content.entity.effect

import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.timer.characterTimerStart
import world.gregs.voidps.engine.timer.characterTimerStop
import world.gregs.voidps.engine.timer.characterTimerTick
import kotlin.math.sign

characterTimerStart("movement_delay") { character ->
    character.start("movement_delay", -1)
    interval = 1
}

characterTimerTick("movement_delay") { character ->
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

characterTimerStop("movement_delay") { character ->
    character.stop("movement_delay")
}
