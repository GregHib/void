package content.skill.summoning

import world.gregs.voidps.engine.timer.timerStart
import world.gregs.voidps.engine.timer.timerStop
import world.gregs.voidps.engine.timer.timerTick
import world.gregs.voidps.engine.event.Script
@Script
class SummoningTimers {

    init {
        timerStart("familiar_timer") {player ->
            interval = 50 // 30 seconds
        
            if(!restart) {
                player["familiar_details_minutes_remaining"] = player.follower!!.def["summoning_time_minutes", 0]
                player["familiar_details_seconds_remaining"] = 0
            }
        }

        timerTick("familiar_timer") {player ->
            if (player["familiar_details_seconds_remaining", 0] == 0) {
                player.dec("familiar_details_minutes_remaining")
            }
            player["familiar_details_seconds_remaining"] = (player["familiar_details_seconds_remaining", 0] + 1) % 2
        
            if (player["familiar_details_seconds_remaining", 0] <= 0 && player["familiar_details_minutes_remaining", 0] <= 0) {
                cancel()
            }
        }

        timerStop("familiar_timer") {player ->
            if (logout) {
                npcs.remove(player.follower)
                return@timerStop
            }
        
            if (player.follower != null) {
                player.dismissFamiliar()
            }
        }

    }

}
