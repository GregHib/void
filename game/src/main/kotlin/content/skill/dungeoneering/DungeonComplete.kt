package content.skill.dungeoneering

import com.github.michaelbull.logging.InlineLogger
import content.area.wilderness.daemonheim.DungeoneeringParty.Companion.dungeonLeader
import content.area.wilderness.daemonheim.DungeoneeringParty.Companion.dungeonMembers
import content.entity.player.dialogue.type.choice
import content.entity.player.modal.Tab
import content.quest.closeTabs
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.Minimap
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.minimap
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Interpolation
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.timer.TICKS
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.area.Rectangle
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.pow

class DungeonComplete : Script {
    private val logger = InlineLogger()

    init {
        objectOperate("End-dungeon", "rand_dungeon_end_trapdoor_unlocked_frozen") {
            if (get("dungeon_move_on_vote", false)) {
                // https://youtu.be/lOQ0CfveKwY?t=710
                message("You have already voted to move on.")
                return@objectOperate
            }
            val leader = dungeonLeader ?: return@objectOperate
            choice("<br>Are you sure you wish to proceed and take your party with you?") {
                option("Yes, continue.") {
                    if (dungeonMembers.size == 1) {
                        dungeonComplete()
                        leader["dungeon_next_timer"] = 30
                        leader.softTimers.startIfAbsent("dungeon_continuation")
                        return@option
                    }
                    set("dungeon_move_on_vote", true)
                    if (leader.contains("dungeon_end_timer")) {
                        val time = dungeonMembers.size * 60
                        set("dungeon_end_timer", time)
                        leader.softTimers.startIfAbsent("dungeon_complete")
                    } else {
                        dec("dungeon_end_timer", 60)
                    }
                    endMessage(leader)
                }
                option("No, wait.")
            }
        }

        npcDeath("rand_ice_lord_boss_*") {
            val room = dungeonRoomBounds()
            var door = findDoor(room.minX - 1, room.minY - 1, "rand_dungeon_end_trapdoor_locked_frozen")
            if (door == null) {
                for (tile in Rectangle(room.minX - 1, room.minY - 1, room.minX + 1, room.minY + 1)) {
                    val obj = GameObjects.findOrNull(tile, "rand_dungeon_end_trapdoor_locked_frozen")
                    if (obj != null) {
                        door = obj
                    }
                }
                logger.warn { "Error finding dungeon door: $door" }
            }
            door?.replace("rand_dungeon_end_trapdoor_unlocked_frozen")
            // TODO rewards (based on combat?)
            // https://youtu.be/nSob5r5-UtE?t=563
            // You received item:

            // https://youtu.be/2aX5poT8Fnk?t=496
            // <username> received item:
        }

        timerStart("dungeon_completion") { TimeUnit.SECONDS.toTicks(15) }

        timerTick("dungeon_completion") {
            val seconds = dec("dungeon_end_timer", 15)
            endMessage(this)
            if (seconds <= 0) {
                Timer.CANCEL
            } else {
                Timer.CONTINUE
            }
        }

        timerStop("dungeon_completion") {
            for (member in dungeonMembers) {
                member.dungeonComplete()
            }
            set("dungeon_next_timer", TimeUnit.SECONDS.toTicks(30))
            nextMessage(this)
            softTimers.startIfAbsent("dungeon_continuation")
        }

        interfaceOption("Ready", "dungeon_complete:readybutton_player*") {
            val button = it.component.removePrefix("readybutton_player").toInt()
            val index = dungeonMembers.indexOf(this) + 1
            if (index != button) {
                return@interfaceOption
            }
            val leader = dungeonLeader ?: return@interfaceOption
            if (leader["dungeon_next_timer", 0] <= 5) {
                // https://youtu.be/zsSofNiDfnw?t=231
                message("It's too late to do that: the next dungeon is about to start.")
                return@interfaceOption
            }
            if (get("rand_ready_state_player${button}", "unset") == "ready") {
                return@interfaceOption
            }
            for (member in dungeonMembers) {
                member["rand_ready_state_player${button}"] = "ready"
            }
            val allReady = (1..dungeonMembers.size).all { i -> get("rand_ready_state_player$i", "unset") != "unset" }
            if (allReady) {
                leader["dungeon_next_timer"] = 0
            }
        }

        timerStart("dungeon_continuation") { 1 }

        timerTick("dungeon_continuation") {
            val ticks = dec("dungeon_next_timer")
            nextMessage(this)
            if (ticks <= 0) {
                Timer.CANCEL
            } else {
                Timer.CONTINUE
            }
        }

    }

    private fun nextMessage(player: Player) {
        val ticks = player["dungeon_next_timer", 0]
        val seconds = TICKS.toSeconds(ticks).toInt()
        if (player["dungeon_next_seconds", 0] == seconds) {
            return
        }
        player["dungeon_next_seconds"] = seconds
        val message = when (seconds) {
            30, 25, 20, 15, 10, 5, 4, 3, 2, 1 -> "Time until next dungeon: $seconds"
            else -> return
        }
        for (member in player.dungeonMembers) {
            member.message(message)
        }
    }

    private fun endMessage(player: Player) {
        val seconds = player["dungeon_end_timer", 0]
        val minutes = TimeUnit.SECONDS.toMinutes(seconds.toLong()).toInt()
        val message = if (minutes == 0) {
            if (seconds == 30 || seconds == 15) {
                "$seconds seconds until dungeon ends."
            } else {
                return
            }
        } else {
            "$minutes ${"minute".plural(minutes)} until dungeon ends."
        }
        for (member in player.dungeonMembers) {
            member.message(message)
        }
    }

    private fun baseFloorXp(floor: Int, size: String): Int {
        // https://www.reddit.com/r/runescape/comments/4ew7kd/has_anyone_figured_out_the_dungeoneering_floor_xp/
        val y = when (size) {
            "large" -> {
                // Extrapolated from dif between medium + small
                0.3208 * floor.toDouble().pow(3) + 13.7022 * floor.toDouble().pow(2) + 55.248 * floor + 1980.23
            }
            "medium" -> {
                // https://imgur.com/qNRsXbA
                0.2404 * floor.toDouble().pow(3) + 6.9911 * floor.toDouble().pow(2) + 66.094 * floor + 1283.8
            }
            else -> {
                // https://imgur.com/C7n8Gqk
                0.16 * floor.toDouble().pow(3) + 0.28 * floor.toDouble().pow(2) + 76.94 * floor + 587.37
            }
        }
        return (y * 10.0).toInt()
    }

    private fun basePrestigeXp(floor: Int, size: String, prestige: Int): Int {
        // https://www.reddit.com/r/runescape/comments/4ew7kd/has_anyone_figured_out_the_dungeoneering_floor_xp/
        val y = if (prestige == 60) {
            when (size) {
                "large" -> 436.2488 * floor + 104288.243 // extrapolated from medium + small
                "medium" -> 284.05 * floor + 68950 // http://i.imgur.com/tPMAmXy.png
                else -> 131.8512 * floor + 33611.757 // https://imgur.com/1my4phK
            }
        } else {
            when (size) {
                "large" -> 7.27081 * prestige * floor + 1738.137 * prestige
                "medium" -> 4.73417 * prestige * floor + 1149.167 * prestige
                else -> 2.19752 * prestige * floor + 560.196 * prestige
            }
        }
        return (y * 10.0).toInt()
    }

    private fun Player.dungeonComplete() {
        set("had_party_open", interfaces.contains("dungeoneering_party"))
        minimap(Minimap.HideMap)
        closeTabs(Tab.Options)
        close("dungeoneering_party")
        // https://youtu.be/brr5Ou1SjVE?t=655
        // item(3028, "The next floor is not available at your Dungeoneering level. Consider resetting your progress to gain the best ongoing rate of xp. Click the advisor button for more information.")
        closeInterfaces()
        open("dungeon_complete")
        for (i in 1..5) {
            set("rand_ready_state_player$i", false, "unset")
            sendVariable("rand_ready_state_player$i")
        }

        // Base XP
        // Floor XP - Depends on floor number, size and difficulty
        // A measure of how much time the floor is expected to take (without using actual time taken)
        val size = get("dungeoneering_party_size", "small")
        val floor = get("dungeoneering_party_floor", 1)
        set("rand_party_current_floor_trans", floor)
        val baseFloorXp = baseFloorXp(floor, size)
        set("rand_basefloor_varc", baseFloorXp) // x10

        // Prestige
        val current = get("dungeoneering_current_progress", 0)
        val previous = get("dungeoneering_previous_progress", 0)
        val prestige = max(current, previous)
        set("dungeon_prestige_current", current)
        set("dungeon_prestige_previous", previous)
        val prestigeXp = basePrestigeXp(floor, size, prestige)
        set("rand_prestige_varc", prestigeXp) // x10
        val totalFloor = (baseFloorXp + prestigeXp) / 2
        set("rand_totalfloor_varc", totalFloor) // x10 Average

        var bonusRooms = 0
        var bonusRoomsOpen = 0
        val dungeon = dungeonMap ?: return
        for (x in 0 until dungeon.width) {
            for (y in 0 until dungeon.height) {
                val room = dungeon.room(x, y) ?: continue
                if (!room.isCritical) {
                    bonusRooms++
                    if (room.open) {
                        bonusRoomsOpen++
                    }
                }
            }
        }

        set("rand_dungeon_size_trans", size)
        val bonus = Interpolation.lerp(bonusRoomsOpen, 0..bonusRooms, 0..10000)
        set("rand_party_bonus_exploration", bonus) // +1%
        set("rand_dungeon_difficulty_trans", dungeonMembers.size * 10 + dungeon.playerCount)
        val complexity = get("dungeoneering_party_complexity", 1)
        set("rand_party_complexity_level_trans", complexity)
        val deaths = get("dungeon_deaths", 0).coerceAtMost(15)
        set("dungeon_deaths", deaths)
        // Unbalanced party penalty x100%
        set("rand_nurf_amount_trans", false)
        val loreStat = 100000
        set("lore_stat_var", loreStat)
        val pointsMod = 2100
        set("rand_pointsmod_varc", pointsMod) // x100

        for (i in 1..5) {
            val member = dungeonMembers.getOrNull(i - 1)
            set("rand_exists_$i", member != null)
            if (member == null) {
                continue
            }
            set("dungeoneering_player_$i", member.name)
            for (i in 1 until 6) {
                set("rand_award_1_$i", if (i > 1) 0 else i)
            }
        }

        val final = computeFinalModifierBasisPoints(this, size, bonus, dungeonMembers.size, dungeon.playerCount, deaths, 0, pointsMod, complexity)
        val totalXp = calculateTotalXp(final, totalFloor)
        val tokens = calculateTokens(final, totalFloor, loreStat)

        if (!get("dungeon_reward_given", false)) {
            println("Reward: ${totalXp} Tokens: $tokens")
            exp(Skill.Dungeoneering, totalXp.toDouble())
            inc("dungeoneering_tokens", tokens.toInt())
            AuditLog.event(this, "completed_dungeon", floor, complexity, dungeonMembers.size, dungeon.playerCount, deaths, totalXp, tokens.toInt())
            set("dungeon_reward_given", true)
        }
        // TODO other messages
        if (get("dungeoneering_guide_mode", false)) {
//            item(3032, "You have now unlocked high complexity within Daemonheim. The complete Dungeoneering experience awaits you on the next floor!") FIXME c2?
        }
        // https://youtu.be/ouT__1cWTTU?t=583
        // https://youtu.be/27ZBYBvBnL0?t=718
//        message("<red>Warning<br>")
//        message("You have already completed all the available floors of this theme and thus cannot be awarded prestige xp until you reset your progress or switch theme.")
        // https://youtu.be/AZtXwFWWiP8?t=251
//        message("Since you have previously completed this floor, floor 24 was instead ticked-off.")
    }

    private fun computeFinalModifierBasisPoints(player: Player, size: String, bonus: Int, partySize: Int, partyDifficulty: Int, deaths: Int, nerf: Int, pointsMod: Int, complexity: Int): Int {
        println("Size=$size, bonus=$bonus, party=$partySize, diff=$partyDifficulty, deaths=$deaths, nerf=$nerf, mod=$pointsMod, complexity=$complexity")
        val sizeBonus = when (size) {
            "medium" -> 792
            "large" -> 1583
            else -> 0
        }
        val complexityBonus = bonus * 1267 / 10000
        val difficultyOffset = xpOffset(partySize, partyDifficulty)
        val deathPenaltyModifier = 10000 - minOf(deaths, 6) * 1000
        val nerfMultiplier = 10000 - nerf

        var chain = 10000L
        chain += sizeBonus
        chain += complexityBonus
        chain += difficultyOffset
        chain += pointsMod
        chain *= complexityModifier(complexity)
        chain /= 10000
        chain *= explorationBonusModifier(player)
        chain /= 10000
        chain *= deathPenaltyModifier
        chain /= 10000
        chain *= nerfMultiplier
        chain /= 10000
        return chain.toInt()
    }

    private fun explorationBonusModifier(player: Player): Int {
        if (player["varc_1196", 0] == 1) {
            return 9000
        }
        return 10000

    }
    private fun complexityModifier(complexity: Int): Int {
        return when (complexity) {
            1 -> 5000
            2 -> 5500
            3 -> 6000
            4 -> 6500
            5 -> 7000
            else -> 10000
        }
    }

    fun calculateTotalXp(finalModifier: Int, before: Int, revealProgress: Int = 100): Int {
        val after = (finalModifier.toLong() * before / 10000).toInt()
        val revealed = Interpolation.lerp(revealProgress, 0..100, before..after)
        return (revealed + 5) / 10
    }

    fun calculateTokens(finalModifier: Int, before: Int, loreStat: Int, revealProgress: Int = 100): Long {
        if (loreStat >= 2_000_000_000) {
            return -1
        }
        val after = (finalModifier.toLong() * before / 10000).toInt()
        val revealed = Interpolation.lerp(revealProgress, 0..100, before..after)
        return revealed.toLong() * 1000 / loreStat
    }

    private fun findDoor(x: Int, y: Int, id: String): GameObject? = GameObjects.findOrNull(Tile(x, y + 7), id) ?: GameObjects.findOrNull(Tile(x + 15, y + 7), id) ?: GameObjects.findOrNull(Tile(x + 7, y), id) ?: GameObjects.findOrNull(Tile(x + 7, y + 15), id)

    companion object {
        private val DIFFICULTY_XP_OFFSETS: Array<Map<Int, Int>> = arrayOf(
            mapOf(1 to 0),
            mapOf(1 to -760, 2 to 507),
            mapOf(1 to -1520, 2 to 190, 3 to 950),
            mapOf(1 to -2280, 2 to -760, 3 to 633, 4 to 1457),
            mapOf(1 to -3040, 2 to -1267, 3 to 380, 4 to 1140, 5 to 1900),
        )

        private fun xpOffset(partySize: Int, partyDifficulty: Int): Int {
            return DIFFICULTY_XP_OFFSETS.getOrNull(partySize - 1)?.get(partyDifficulty) ?: 0
        }
    }
}
