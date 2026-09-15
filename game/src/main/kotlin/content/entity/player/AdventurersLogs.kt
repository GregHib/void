package content.entity.player

import content.entity.player.command.find
import content.quest.questJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.playerCommand
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.chat.toDigitGroupString
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.RecentEvent
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.data.definition.FontDefinitions
import world.gregs.voidps.engine.data.definition.QuestDefinitions
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.isAdmin
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timer.epochSeconds
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class AdventurersLogs(
    val accounts: AccountDefinitions,
    val fonts: FontDefinitions,
) : Script {
    init {
        experience { skill, from, to ->
            if (to < 14_000_000 && from / 1_000_000 != to / 1_000_000) {
                val xp = to - to.rem(1_000_000)
                logEvent("${xp}XP in ${skill.name}", "I now have at least ${xp.toDigitGroupString()} experience points in the ${skill.name} skill.")
            } else if (to <= 50_000_000 && from / 5_000_000 != to / 5_000_000) {
                val xp = to - to.rem(5_000_000)
                logEvent("${xp}XP in ${skill.name}", "I now have at least ${xp.toDigitGroupString()} experience points in the ${skill.name} skill.")
            } else if (from / 10_000_000 != to / 10_000_000) {
                val xp = to - to.rem(10_000_000)
                logEvent("${xp}XP in ${skill.name}", "I now have at least ${xp.toDigitGroupString()} experience points in the ${skill.name} skill.")
            }
        }

        maxLevelChanged { skill, from, to ->
            if (from >= to || isAdmin()) {
                return@maxLevelChanged
            }
            val total = Skill.all.sumOf { (if (it == Skill.Constitution) levels.getMax(it) / 10 - 10 else levels.getMax(it) - 1) }
            if (total == 2496 || total == 100 || total == 150 || total == 250 || total == 500 || total == 1000 || total == 1500 || total == 2000) {
                logEvent("$total total levels gained", "I now have a total level of $total, split among all my skills.")
                return@maxLevelChanged
            }
            val min = Skill.all.minOf { (if (it == Skill.Constitution) levels.getMax(it) / 10 else levels.getMax(it)) }
            if (min == to) {
                logEvent("Levelled all skills over $min", "By levelling up my ${skill.name} skill, I achieved at least level $to in all skills.")
                return@maxLevelChanged
            }

            logEvent("Levelled up ${skill.name}", "I levelled my ${skill.name} skill, I am now level $to.")
        }

        playerCommand(
            "adventurers_log",
            stringArg("player-name", optional = true, autofill = accounts.displayNames.keys),
            desc = "Check the adventurer's logs of a player that's online (blank for self)",
            handler = ::listLogs,
        )
    }

    private val formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm")

    private fun listLogs(player: Player, args: List<String>) {
        val target = Players.find(player, args.getOrNull(0)) ?: return
        if (player.hasClock("commands_delay")) {
            return
        }
        player.start("commands_delay", 1)
        val list = mutableListOf<String>()
        val time = player["playtime", 0].toLong()
        val days = TimeUnit.SECONDS.toDays(time)
        val hours = TimeUnit.SECONDS.toDays(time).rem(24)
        list.add("<maroon>${target.name}")
        list.add("<blue>Time Played:</col> $days ${"Day".plural(days)} $hours ${"Hour".plural(hours)}")
        list.add("")
        list.add("<blue>My Recent Events")
        if (target.recentEvents.isEmpty()) {
            list.add("No recent events.")
            list.add("I don't have any recent events yet. I need to do more adventuring.")
        } else {
            val font = fonts.get("p12_full")
            for (event in target.recentEvents.reversed()) {
                val instant = Instant.ofEpochSecond(event.time.toLong())
                val dateTime = instant.atZone(ZoneId.systemDefault())
                val timestamp = dateTime.format(formatter)
                list.add("<navy>${event.title}")
                for (line in font.splitLines(event.description, 400)) {
                    list.add(line)
                }
                list.add("($timestamp)")
                list.add("")
            }
        }
        player.questJournal("Adventurer's Log", list)
    }

    companion object {
        fun questCompleted(player: Player, quest: String, points: Int) {
            val def = get<QuestDefinitions>().getOrNull(quest) ?: return
            val message = Tables.stringOrNull("quest_messages.$quest.message") ?: "Quest Completed!"
            player.logEvent("Quest complete: ${def.name}", message)
            if (points != 0) {
                val from = player["quest_points", 0]
                val to = from + points
                var milestone = -1
                for (ms in QP_MILESTONES) {
                    if (ms in (from + 1)..to) {
                        milestone = ms
                    }
                }
                if (milestone != -1) {
                    player.logEvent("$to Quest points obtained", "Completing ${def.name} has given me enough Quest Points to pass the $milestone QP milestone.")
                }
            }
        }

        private val QP_MILESTONES = setOf(12, 21, 33, 44, 50, 56, 101, 108, 176, 200, 250)
    }
}

fun Player.logEvent(title: String, description: String) {
    if (recentEvents.size >= Settings["world.players.adventurerLogsCount", 10]) {
        recentEvents.removeFirst()
    }
    recentEvents.add(RecentEvent(epochSeconds(), title, description))
}
