package content.entity.obj

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.event.Script
@Script
class Boxes {

    init {
        objectOperate("Search", "lumbridge_boxes") {
            player.message("There is nothing interesting in these boxes.")
        }

    }

}
