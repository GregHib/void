package content.skill.dungeoneering

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.skill.level.Interpolation

class DungeonComplete : Script {
    init {
        adminCommand("complete") {
            closeInterfaces()
            open("dungeon_complete")

            // Base XP
            // Floor
            set("rand_party_current_floor_trans", get("dungeon_party_floor", 1))
            set("rand_basefloor_varc", 30) // x10
            set("rand_totalfloor_varc", 2000) // x10 Average

            // Prestige
            set("dungeon_prestige_current", get("dungeoneering_current_progress", 0))
            set("dungeon_prestige_previous", get("dungeoneering_previous_progress", 0))
            set("rand_prestige_varc", 40) // x10

            // Dungeon size
            set("rand_dungeon_size_trans", "large")

            // Bonus rooms
            set("rand_party_bonus_exploration", Interpolation.lerp(2, 0..13, 0..10000)) // +1%

            // Difficulty
            set("rand_dungeon_difficulty_trans", 105) // 7:1

            // Complexity
            set("rand_party_complexity_level_trans", get("dungeon_party_complexity", 1))

            // Deaths
            set("dungeon_deaths", 0.coerceAtMost(15))
            // Unbalanced party penalty x100%
            set("rand_nurf_amount_trans", false)

            // height = (10000 + dungeonSizeHeight + bonusHeight
            // (totalfloor + 100 * (height - totalFloor) / 100 + 5) / 100
            set("lore_stat_var", 100000)
            set("rand_pointsmod_varc", 2100) // x100

            set("rand_exists_1", true)
            set("dungeoneering_player_1", "player_1")
            for (i in 1 until 6) {
                set("rand_award_1_${i}", if (i > 1) 0 else i)
            }

            set("rand_exists_2", true)
            set("dungeoneering_player_2", "player_2")
            for (i in 1 until 6) {
                set("rand_award_2_${i}", if (i > 2) 0 else i)
            }

            set("rand_exists_3", true)
            set("dungeoneering_player_3", "player_3")
            for (i in 1 until 6) {
                set("rand_award_3_${i}", if (i > 3) 0 else i)
            }

            set("rand_exists_4", true)
            set("dungeoneering_player_4", "player_4")
            for (i in 1 until 6) {
                set("rand_award_4_${i}", if (i > 4) 0 else i)
            }

            set("rand_exists_5", true)
            set("dungeoneering_player_5", "player_5")
            for (i in 1 until 6) {
                set("rand_award_5_${i}", if (i > 5) 0 else i)
            }
        }
    }
}