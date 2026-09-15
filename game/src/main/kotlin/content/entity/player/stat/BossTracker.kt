package content.entity.player.stat

import content.entity.combat.damageDealers
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.playerCommand
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
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

        npcLevelChanged(Skill.Constitution) { skill, from, to ->
            val categories: Set<String> = def.getOrNull("categories") ?: return@npcLevelChanged
            if (!categories.contains("boss")) {
                return@npcLevelChanged
            }
            val max = levels.getMax(skill)
            if (to == max && from != max) { // Reset if healed back to max
                clear("${id}_kill_timer")
            } else if (from == max && to != max) { // Start when hit down from max
                set("${id}_kill_timer", epochMilliseconds())
            } else if (to <= 0 && from > 0) {
                val count = damageDealers.size
                val start = get("${id}_kill_timer", 0L)
                for ((char, damage) in damageDealers) {
                    if (char is Player && damage > 0) {
                        kill(char, "Your ${def.name} kill count is", "${id}_kills")
                        record(char, start, id, count, "Fight duration")
                    }
                }
            }
        }
    }

    companion object {
        fun record(player: Player, start: Long, timer: String, teamSize: Int, prefix: String = "Duration") {
            val duration = epochMilliseconds() - start
            val type = when (teamSize) {
                0 -> return
                1 -> "solo"
                2 -> "duo"
                3 -> "trio"
                4 -> "quad"
                else -> "mass"
            }
            val best = player["${timer}_fastest_${type}", 0L]
            if (duration > best && !player["insta_kill", false] && !player["god_mode", false]) { // No cheating!
                player["${timer}_fastest_${type}"] = duration.toInt()
            }
            if (player["boss_timers", false]) {
                val time = "<red>${timestamp(duration)}</col>"
                val teamPrefix = if (teamSize > 1) "Team size: <red>${teamSize} players</col> " else ""
                if (duration > best) {
                    player.message("$teamPrefix$prefix: $time (new personal best)")
                } else {
                    player.message("$teamPrefix$prefix: $time. Personal best: ${timestamp(best)}")
                }
            }
        }

        fun start(player: Player, timer: String) {
            player["${timer}_kill_timer"] = epochMilliseconds()
        }

        fun stop(player: Player, timer: String, teamSize: Int = 1, prefix: String = "Duration") {
            val start = player["${timer}_kill_timer", 0L]
            if (start == 0L) {
                return
            }
            record(player, start, timer, teamSize, prefix)
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
                "$hours:$minutes:$seconds"
            } else if (minutes > 0) {
                "$minutes:$seconds"
            } else {
                "$minutes:$seconds.$millis"
            }
        }
    }
}