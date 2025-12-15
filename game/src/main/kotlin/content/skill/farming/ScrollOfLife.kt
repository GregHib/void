package content.skill.farming

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.random

object ScrollOfLife {
    fun checkLife(player: Player, type: String, chop: Boolean = false) {
        if (!player["scroll_of_life", false]) {
            return
        }
        if (type == "calquat" || type == "spirit_tree") {
            return
        }
        if (random.nextInt(100) < if (type == "tree") 5 else 10) {
            player.inventory.add("${type}_seed") // TODO proper seed
            if (Settings["world.additional.messages", false]) {
                if (chop) {
                    player.message("<green>As the farmer chops the tree down, you spot a seed in the leftovers.")
                } else {
                    player.message("<green>Your Scroll of Life saves you a seed!")
                }
            }
//            player.message("The secret is yours! You read the scroll and unlock the long-lost technique of regaining seeds from dead farming patches.")
        }
    }
}
