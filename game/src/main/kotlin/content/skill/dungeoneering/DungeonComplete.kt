package content.skill.dungeoneering

import com.github.michaelbull.logging.InlineLogger
import content.area.wilderness.daemonheim.DungeoneeringParty.Companion.dungeonLeader
import content.area.wilderness.daemonheim.DungeoneeringParty.Companion.dungeonMembers
import content.entity.player.dialogue.type.choice
import content.quest.clearInstance
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.level.Interpolation
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.area.Rectangle
import java.util.concurrent.TimeUnit

class DungeonComplete : Script {
    private val logger = InlineLogger()

    init {
        objectOperate("End-dungeon", "rand_dungeon_end_trapdoor_unlocked_frozen") {
            // TODO leader only?
            choice("Are you sure you wish to proceed and take your party with you?") {
                option("Yes, continue.") {
                    if (dungeonMembers.size == 1) {
                        dungeonComplete()
                    } else {
                        val leader = dungeonLeader ?: return@option
                        if (!leader.contains("dungeon_complete_timer")) {
                            val time = dungeonMembers.size * 60
                            set("dungeon_complete_timer", time)
                        }
                        leader.softTimers.startIfAbsent("dungeon_complete")
                    }
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
        }

        timerStart("dungeon_completion") { 30 }

        timerTick("dungeon_completion") {
            val seconds = dec("dungeon_complete_timer", 30)
            message(seconds)
            if (seconds == 0) {
                Timer.CANCEL
            } else {
                Timer.CONTINUE
            }
        }

        timerStop("dungeon_completion") {
            // TODO
        }

        interfaceOption("Ready", "dungeon_complete:readybutton_player1") {
            exit()
        }
    }

    private fun Player.exit() {
        // TODO clear inv etc..
        tele(3460, 3721, 1)
        clearInstance() // TODO remove
    }

    private fun message(seconds: Int) {
        val minutes = TimeUnit.SECONDS.toMinutes(seconds.toLong()).toInt()
        // TODO broadcast
        // 2 minutes until dungeon ends.
        // 1 minute until dungeon ends.
        // 30 seconds until dungeon ends.
        // 15 seconds until dungeon ends.
        // Time until next dungeon: 30 (25, 20, 15, 10, 5, 4, 3, 2, 1)
    }

    private fun Player.dungeonComplete() {
        // https://youtu.be/brr5Ou1SjVE?t=655
        // item(3028, "The next floor is not available at your Dungeoneering level. Consider resetting your progress to gain the best ongoing rate of xp. Click the advisor button for more information.")

        closeInterfaces()
        open("dungeon_complete")

        // Base XP
        // Floor
        set("rand_party_current_floor_trans", get("dungeoneering_party_floor", 1))
        val baseFloor = 40
        set("rand_basefloor_varc", baseFloor) // x10

        // Prestige
        val prestige = 648
        set("dungeon_prestige_current", get("dungeoneering_current_progress", 0))
        set("dungeon_prestige_previous", get("dungeoneering_previous_progress", 0))
        set("rand_prestige_varc", prestige) // x10

        set("rand_totalfloor_varc", (baseFloor + prestige) / 2) // x10 Average

        // Dungeon size
        set("rand_dungeon_size_trans", get("dungeoneering_party_size", "small"))

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

        // Bonus rooms
        set("rand_party_bonus_exploration", Interpolation.lerp(bonusRoomsOpen, 0..bonusRooms, 0..10000)) // +1%

        // Difficulty
        set("rand_dungeon_difficulty_trans", 11) // 71 = 7:1

        // Complexity
        set("rand_party_complexity_level_trans", get("dungeoneering_party_complexity", 1))

        // Deaths
        set("dungeon_deaths", get("dungeon_deaths", 0).coerceAtMost(15))
        // Unbalanced party penalty x100%
        set("rand_nurf_amount_trans", false)

        // height = (10000 + dungeonSizeHeight + bonusHeight
        // (totalfloor + 100 * (height - totalFloor) / 100 + 5) / 100
        set("lore_stat_var", 100000)
        set("rand_pointsmod_varc", 2100) // x100

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

        // TODO other messages

        if (get("dungeoneering_guide_mode", false)) {
//            item(3032, "You have now unlocked high complexity within Daemonheim. The complete Dungeoneering experience awaits you on the next floor!") FIXME c2?
        }
    }

    private fun findDoor(x: Int, y: Int, id: String): GameObject? = GameObjects.findOrNull(Tile(x, y + 7), id) ?: GameObjects.findOrNull(Tile(x + 15, y + 7), id) ?: GameObjects.findOrNull(Tile(x + 7, y), id) ?: GameObjects.findOrNull(Tile(x + 7, y + 15), id)
}
