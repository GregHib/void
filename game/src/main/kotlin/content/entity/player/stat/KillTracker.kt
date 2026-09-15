package content.entity.player.stat

import content.entity.combat.damageDealers
import content.entity.combat.killer
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.playerCommand
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.timer.epochMilliseconds
import java.util.concurrent.TimeUnit

class KillTracker : Script {
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
            if (to == max && from != max) { // Reset when healed back to max
                clear("${id}_kill_timer")
            } else if (from == max && to != max) { // Start when hit down from max
                set("${id}_kill_timer", epochMilliseconds())
            } else if (to <= 0 && from > 0) { // Stop when dead
                val count = damageDealers.size
                val start = get("${id}_kill_timer", 0L)
                for ((char, damage) in damageDealers) {
                    if (char is Player && damage > 0) {
                        record(char, start, id, count, "Fight duration")
                    }
                }
                clear("${id}_kill_timer")
            }
        }

        npcDeath {
            val player = killer as? Player ?: return@npcDeath
            val categories: Set<String> = def.getOrNull("categories") ?: return@npcDeath
            for (category in categories) {
                if (category == "boss") {
                    count(player, id, "Your ${def.name} kill count is")
                } else {
                    count(player, category)
                }
            }
        }
    }

    companion object {
        fun record(player: Player, start: Long, timer: String, teamSize: Int, prefix: String = "Duration") {
            val duration = epochMilliseconds() - start
            if (TimeUnit.MILLISECONDS.toHours(duration) > 500) {
                return // Would exceed integer storage
            }
            val key = when (teamSize) {
                0 -> return
                1 -> timer
                2 -> "${timer}_duo"
                3 -> "${timer}_trio"
                4 -> "${timer}_quad"
                else -> "${timer}_mass"
            }
            val best = player.records.getOrDefault(key, 0)
            if (duration > best && !player["insta_kill", false] && !player["god_mode", false]) { // No cheating!
                player.records[key] = duration.toInt()
            }
            if (player["boss_timers", false]) {
                val time = "<red>${timestamp(duration)}</col>"
                val teamPrefix = if (teamSize > 1) "Team size: <red>${teamSize} players</col> " else ""
                if (duration > best) {
                    player.message("$teamPrefix$prefix: $time (new personal best)")
                } else {
                    player.message("$teamPrefix$prefix: $time. Personal best: ${timestamp(best.toLong())}")
                }
            }
        }

        fun start(player: Player, timer: String) {
            player[timer] = epochMilliseconds()
        }

        fun stop(player: Player, timer: String, teamSize: Int = 1, prefix: String = "Duration") {
            val start = player[timer, 0L]
            if (start == 0L) {
                return
            }
            player.clear(timer)
            record(player, start, timer, teamSize, prefix)
        }

        fun count(player: Player, category: String, prefix: String? = null) {
            val count = player.kills.getOrDefault(category, 0) + 1
            player.kills[category] = count
            if (player["kill_counts", false] && prefix != null) {
                player.message("$prefix: <red>$count<col>.")
            }
        }

        private fun timestamp(duration: Long): String {
            val hours = TimeUnit.MILLISECONDS.toHours(duration)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(duration).rem(60)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(duration).rem(60)
            val millis = duration.rem(1000) / 10
            return if (hours > 0) {
                "$hours:$minutes:$seconds"
            } else {
                "$minutes:$seconds.$millis"
            }
        }
    }
}
