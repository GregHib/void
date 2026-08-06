package content.skill.dungeoneering

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.ui.open

class DungeonComplete : Script {
    init {
        adminCommand("complete") {
            closeInterfaces()
            open("dungeon_complete")

            // Base XP
            // Floor
            set("rand_party_current_floor_trans", 20)
            set("rand_basefloor_varc", 30) // x10

            // Prestige
            set("dungeon_prestige_current", 10)
            set("dungeon_prestige_previous", 11)
            set("rand_prestige_varc", 12)

            // Dungeon size
            set("rand_dungeon_size_trans", 3)

            // Bonus rooms
            set("rand_party_bonus_exploration", 1000) // +1%

            // Difficulty
            set("rand_dungeon_difficulty_trans", 71) // 7:1

            // Complexity
            set("rand_party_complexity_level_trans", 6)

            // Deaths
            set("dungeon_deaths", 2)
            // Unbalanced party penalty x100%
            set("rand_nurf_amount_trans", 0)

            // height = (10000 + dungeonSizeHeight + bonusHeight
            // (totalfloor + 100 * (height - totalFloor) / 100 + 5) / 100
            set("lore_stat_var", 0)
            set("rand_totalfloor_varc", 1000)
            set("rand_pointsmod_varc", 200) //+1%?
        }
    }
}