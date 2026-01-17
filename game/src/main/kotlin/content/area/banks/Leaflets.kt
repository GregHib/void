package content.area.banks

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

class Leaflets : Script {

    init {
        objectOperate("Take", "*_bank_leaflet") {
            if (inventory.contains("leaflet")) {
                message("You already have a copy of the leaflet.")
            } else {
                inventory.add("leaflet")
            }
        }
    }
}
