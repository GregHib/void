package content.area.asgarnia.port_sarim

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.inv.holdsItem

class PortSarim : Script {

    init {
        takeable("white_apron_port_sarim") {
            if (holdsItem("white_apron")) {
                message("You already have one of those.")
                null
            } else {
                "white_apron"
            }
        }

        taken("white_apron_port_sarim") {
            anim("take")
            message("You take an apron. It feels freshly starched and smells of laundry.")
        }
    }
}
