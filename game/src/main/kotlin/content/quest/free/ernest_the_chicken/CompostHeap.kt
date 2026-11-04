package content.quest.free.ernest_the_chicken

import content.entity.player.bank.ownsItem
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

class CompostHeap : Script {
    init {
        objectOperate("Search", "ernest_compost") { searchCompost() }

        itemOnObjectOperate("spade", "ernest_compost") { searchCompost() }

        itemOption("Dig", "spade") {
            if (tile.level == 0 && tile.x in 3083..3086 && tile.y in 3359..3362) {
                searchCompost()
            }
        }
    }

    private suspend fun Player.searchCompost() {
        if (!inventory.contains("spade")) {
            message("You need a spade to search the compost heap properly.")
            return
        }
        if (ownsItem("grimy_key")) {
            message("You have already found the key.")
            return
        }
        message("You dig through the compost...")
        anim("ernest_dig")
        delay(2)
        if (inventory.add("grimy_key")) {
            message("... and find a small key.")
        } else {
            message("You don't have enough inventory space to take the key.")
        }
    }
}
