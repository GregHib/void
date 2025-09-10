package content.area.kandarin.catherby

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnItem
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.event.Script
@Script
class FishBowl {

    init {
        itemOnItem("fishbowl_water", "seaweed") { player ->
            if (player.inventory.contains("fishbowl_water") && player.inventory.contains("seaweed")) {
                player.inventory.remove("fishbowl_water", 1)
                player.inventory.remove("seaweed", 1)
                player.inventory.add("fishbowl_seaweed")
                player.message("You carefully place the seaweed into the fishbowl.")
            }
        }

    }

}
