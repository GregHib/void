package content.skill.summoning

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

class SummoningTimers : Script {

    init {
        timerStart("familiar_timer") { restart ->
            if (!restart) {
                set("familiar_details_minutes_remaining", follower!!.def["summoning_time_minutes", 0])
                set("familiar_details_seconds_remaining", 0)
            }
            return@timerStart TimeUnit.SECONDS.toTicks(30)
        }

        timerTick("familiar_timer") {
            var seconds = get("familiar_details_seconds_remaining", 0)
            if (seconds == 0) {
                dec("familiar_details_minutes_remaining")
            }
            seconds = (seconds + 1) % 2
            set("familiar_details_seconds_remaining", seconds)
            val minutes = get("familiar_details_minutes_remaining", 0)
            if (minutes == 1 && seconds == 0) {
                message("You have 1 minute before your familiar vanishes.")
            } else if (minutes == 0 && seconds == 1) {
                message("You have 30 seconds before your familiar vanishes.")
            }
            if (seconds <= 0 && minutes <= 0) {
                return@timerTick Timer.CANCEL
            }

            return@timerTick Timer.CONTINUE
        }

        timerStop("familiar_timer") { logout ->
            if (logout) {
                NPCs.remove(follower)
                return@timerStop
            }

            if (follower != null) {
                dismissFamiliar()
            }
        }
    }
}
