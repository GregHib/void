package content.area.misthalin.lumbridge

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.type.random

class LumbridgeBookcase : Script {

    init {
        objectOperate("Search", "lumbridge_bookcase") {
            message("You search the books...")
            delay(2)
            when (random.nextInt(0, 3)) {
                0 -> message("None of them look very interesting.")
                1 -> message("You find nothing to interest you.")
                2 -> message("You don't find anything that you'd ever want to read.")
            }
        }
    }
}
