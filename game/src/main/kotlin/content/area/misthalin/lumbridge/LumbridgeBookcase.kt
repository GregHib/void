package content.area.misthalin.lumbridge

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.type.random

@Script
class LumbridgeBookcase {

    init {
        objectOperate("Search", "lumbridge_bookcase") {
            player.message("You search the books...")
            delay(2)
            when (random.nextInt(0, 3)) {
                0 -> player.message("None of them look very interesting.")
                1 -> player.message("You find nothing to interest you.")
                2 -> player.message("You don't find anything that you'd ever want to read.")
            }
        }
    }
}
