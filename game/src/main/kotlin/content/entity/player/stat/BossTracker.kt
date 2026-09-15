package content.entity.player.stat

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.playerCommand
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.timer.epochMilliseconds
import java.util.concurrent.TimeUnit

class BossTracker : Script {
    init {
        playerCommand("boss_timers", desc = "Toggle whether boss kill timers are displayed") {
            message("Boss timers are now: ${if (toggle("boss_timers")) "enabled" else "disabled"}.")
        }

        playerCommand("kill_counts", desc = "Toggle whether boss kill counts are displayed") {
            message("Boss kill counts are now: ${if (toggle("kill_counts")) "enabled" else "disabled"}.")
        }
    }

    companion object {
        fun start(player: Player, timer: String) {
            player["${timer}_timer"] = epochMilliseconds()
        }

        fun stop(player: Player, timer: String, prefix: String = "Duration") {
            val start = player["${timer}_timer", 0L]
            if (start == 0L) {
                return
            }
            val duration = epochMilliseconds() - start
            val best = player["${timer}_fastest", 0L]
            if (duration > best) {
                player["${timer}_fastest"] = duration
            }
            if (player["boss_timers", false]) {
                val time = "<red>${timestamp(duration)}</col>"
                if (duration > best) {
                    player.message("$prefix: $time (new personal best)")
                } else {
                    player.message("$prefix: $time. Personal best: ${timestamp(best)}")
                }
            }
        }

        fun kill(player: Player, prefix: String, tracker: String) {
            val kills = player.inc(tracker)
            if (player["kill_counts", false]) {
                player.message("$prefix: <red>$kills<col>.")
            }
        }

        private fun timestamp(duration: Long): String {
            val hours = TimeUnit.MILLISECONDS.toHours(duration)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(duration).rem(60)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(duration).rem(60)
            val millis = duration.rem(1000) / 10
            return if (hours > 0) {
                "$hours:$minutes:$seconds.$millis"
            } else {
                "$minutes:$seconds.$millis"
            }
        }
    }
}