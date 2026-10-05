package content.entity.obj

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message

class Crates : Script {

    init {
        objectOperate("Search", "crate_307") {
            message("You search the crates but find nothing.")
        }
    }
}
