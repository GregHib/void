package content.area.banks

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.event.Script
@Script
class Leaflets {

    init {
        objectOperate("Take", "*_bank_leaflet") {
            if (player.inventory.contains("leaflet")) {
                player.message("You already have a copy of the leaflet.")
            } else {
                player.inventory.add("leaflet")
            }
        }

    }

}
