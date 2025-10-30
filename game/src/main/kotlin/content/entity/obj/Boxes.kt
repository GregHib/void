package content.entity.obj

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.obj.objectOperate

class Boxes : Script {

    init {
        objectOperate("Search", "lumbridge_boxes") {
            player.message("There is nothing interesting in these boxes.")
        }
    }
}
