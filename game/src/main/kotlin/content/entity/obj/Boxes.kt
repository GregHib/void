package content.entity.obj

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message

class Boxes : Script {

    init {
        objectOperate("Search", "lumbridge_boxes") {
            message("There is nothing interesting in these boxes.")
        }
    }
}
