package content.area.misthalin.lumbridge.farm

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message

class Sacks : Script {
    init {
        // both rs3 and osrs has this same message.
        objectOperate("Search", "sacks_*") {
            message("There's nothing interesting in these sacks.")
        }
    }
}
