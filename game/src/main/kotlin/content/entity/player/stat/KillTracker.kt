package content.entity.player.stat

import content.entity.combat.damageDealers
import content.entity.combat.killer
import content.entity.player.command.find
import content.entity.player.logEvent
import content.quest.questJournal
import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.playerCommand
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.an
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.chat.toDigitGroupString
import world.gregs.voidps.engine.client.variable.PlayerVariables
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.timer.epochMilliseconds
import java.util.concurrent.TimeUnit

class KillTracker(val accounts: AccountDefinitions) : Script {
    init {
        playerCommand("boss_timers", desc = "Toggle whether boss kill timers are displayed") {
            message("Boss timers are now: ${if (toggle("boss_timers")) "enabled" else "disabled"}.")
        }

        playerCommand("kill_counts", desc = "Toggle whether boss kill counts are displayed") {
            message("Boss kill counts are now: ${if (toggle("kill_counts")) "enabled" else "disabled"}.")
        }

        val categories = NPCDefinitions.definitions.flatMap { it.getOrNull<Set<String>>("categories") ?: emptySet() }.toSet() + NPCDefinitions.ids.keys
        playerCommand(
            "kills",
            stringArg("category", optional = true, autofill = categories),
            stringArg("player-name", optional = true, autofill = accounts.displayNames.keys),
            desc = "Check number of kills for a given npc category",
            handler = ::listKills,
        )

        playerCommand(
            "time_records",
            stringArg("category", optional = true, autofill = categories),
            stringArg("player-name", optional = true, autofill = accounts.displayNames.keys),
            desc = "Check number of personal best kill counts for a given npc category",
            handler = ::listRecords,
        )

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
            // Temp store kill counts to write to player on logout
            player.inc("kills_$id")
        }

        playerDespawn {
            val vars = variables as? PlayerVariables ?: return@playerDespawn
            val temp = vars.temp

            val rex = temp["kills_dagannoth_rex"] as? Int ?: 0
            val prime = temp["kills_dagannoth_prime"] as? Int ?: 0
            val supreme = temp["kills_dagannoth_supreme"] as? Int ?: 0
            val kings = rex + prime + supreme
            if (kings > 0) {
                logEvent("I killed ${if (kings > 1) kings else "a"} Dagannoth ${"King".plural(kings)}", "I killed $kings Dagannoth ${"King".plural(kings)}.")
            }

            val icyBones = temp.filter { it.key.startsWith("kills_rand_ice_lord_boss") }.values.filterIsInstance<Int>().sum()
            if (icyBones > 0) {
                logEvent("I killed $icyBones boss ${"monster".plural(icyBones)} in Daemonheim.", "I killed $icyBones boss ${"monster".plural(icyBones)} called: Icy Bones in Daemonheim.")
            }

            for (key in temp.keys.filter { it.startsWith("kills_") }) {
                val count = temp[key] as? Int ?: continue
                val id = key.substringAfter("kills_")
                val rows = Rows.getOrNull("log_npcs.$id") ?: continue
                var message = if (count == 1) {
                    rows.stringOrNull("single") ?: rows.stringOrNull("multiple")
                } else {
                    rows.stringOrNull("multiple")
                } ?: continue
                val name = NPCDefinitions.get(id).name
                logEvent(
                    "I killed${if (count > 1) " $count" else name.an()} ${name.plural(count)}.",
                    message
                        .replace("<count>", if (count >= 100) "a great number of" else count.toString())
                        .replace("<name>", name.plural(count)),
                )
            }
        }
    }

    private fun listKills(player: Player, args: List<String>) {
        val target = Players.find(player, args.getOrNull(1)) ?: return
        val category = args.getOrNull(0)
        if (category != null) {
            val count = target.kills[category]
            if (count == null) {
                player.message("No kills found for category '$category'.", ChatType.Console)
            } else {
                player.message("Kill count: $count for category: '$category'.", ChatType.Console)
            }
            return
        }
        if (player.hasClock("commands_delay")) {
            return
        }
        player.start("commands_delay", 1)
        val list = target.kills.toList().sortedByDescending { it.second }.map { "${it.first.toTitleCase()} = ${it.second.toDigitGroupString()}" }
        player.questJournal("NPC Kills List", list)
    }

    private fun listRecords(player: Player, args: List<String>) {
        val target = Players.find(player, args.getOrNull(1)) ?: return
        val category = args.getOrNull(0)
        if (player.hasClock("commands_delay")) {
            return
        }
        val records = if (category != null) {
            val count = target.records.filter { it.key.startsWith(category) }
            if (count.isEmpty()) {
                player.message("No records found for category '$category'.", ChatType.Console)
                return
            }
            count
        } else {
            target.records
        }
        player.start("commands_delay", 1)
        val list = records.toList().sortedBy { it.second }.map {
            val name = it.first.replace("_duo", " (Duo)").replace("_trio", " (Trio)").replace("_quad", " (Quad)").replace("_mass", " (Mass)")
            "${name.toTitleCase()}: ${timestamp(it.second.toLong())}"
        }
        player.questJournal("Time Records List", list)
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
                val teamPrefix = if (teamSize > 1) "Team size: <red>$teamSize players</col> " else ""
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
