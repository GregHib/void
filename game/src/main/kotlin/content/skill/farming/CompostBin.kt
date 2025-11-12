package content.skill.farming

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class CompostBin : Script {
    init {
        itemOnObjectOperate("weeds", "compost_bin_empty,farming_compost_bin_#") {
            anim("take")
            sound("farming_putin")
            delay(2)
            val count = inventory.count("weeds")
            inventory.remove("weeds", count)
            repeat(count) {
                delay(2)
            }
            message("This compost bin contains compostable items (3/15).")
        }
    }
}
