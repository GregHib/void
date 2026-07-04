package content.skill.hunter

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class Salamander : Script {
    init {
        itemOption("Release", "orange_salamander,red_salamander,black_salamander,green_salamander") { (item) ->
            inventory.remove(item.id)
            message("You release the salamander and it darts away.", ChatType.Filter)
        }
    }
}