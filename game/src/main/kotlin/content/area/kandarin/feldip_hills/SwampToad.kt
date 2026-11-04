package content.area.kandarin.feldip_hills

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit

class SwampToad : Script {

    init {
        npcSpawn("swamp_toad_feldip_hills") {
            softTimers.start("swamp_toad_sound")
        }

        npcTimerStart("swamp_toad_sound") {
            TimeUnit.SECONDS.toTicks(random.nextInt(15, 41))
        }

        npcTimerTick("swamp_toad_sound") {
            when (random.nextInt(3)) {
                0 -> say("Ribbit")
                1 -> say("Croak")
            }
            Timer.CONTINUE
        }
    }
}
