package content.area.misthalin.lumbridge.roddecks_house

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

class RoddecksBookcase : Script {

    init {
        objectOperate("Search", "roddecks_bookcase") {
            if (inventory.contains("roddecks_diary") && inventory.contains("manual_unstable_foundations")) {
                message("There's nothing particularly interesting here.")
            } else {
                if (!inventory.contains("roddecks_diary")) {
                    inventory.add("roddecks_diary")
                }
                if (!inventory.contains("manual_unstable_foundations")) {
                    inventory.add("manual_unstable_foundations")
                }
            }
        }
    }
}
