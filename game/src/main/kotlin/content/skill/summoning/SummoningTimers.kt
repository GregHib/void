package content.skill.summoning

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

class SummoningTimers : Script {

    init {
        timerStart("familiar_timer") { restart ->
        
            if(!restart) {
                set("familiar_details_minutes_remaining", follower!!.def["summoning_time_minutes", 0])
                set("familiar_details_seconds_remaining", 0)
            }

            return@timerStart TimeUnit.SECONDS.toTicks(30)
        }

        timerTick("familiar_timer") {
            if (get("familiar_details_seconds_remaining", 0) == 0) {
                dec("familiar_details_minutes_remaining")
            }
            set("familiar_details_seconds_remaining", (get("familiar_details_seconds_remaining", 0) + 1) % 2)
        
            if (get("familiar_details_seconds_remaining", 0) <= 0 && get("familiar_details_minutes_remaining", 0) <= 0) {
                return@timerTick Timer.CANCEL
            }

            return@timerTick Timer.CONTINUE
        }

        timerStop("familiar_timer") { logout ->
            if (logout) {
                npcs.remove(follower)
                return@timerStop
            }
        
            if (follower != null) {
                dismissFamiliar()
            }
        }

    }

}
