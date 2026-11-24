package content.area.fremennik_province.lighthouse

import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.obj.replace

class LighthouseDoor : Script {

    init {
        objectOperate("Walk-through", "lighthouse_doorway") { (target) ->
            if (!questCompleted("horror_from_the_deep")) {
                message("You can't see any way to open the door.")
                return@objectOperate
            }
            target.replace("lighthouse_doorway_open", ticks = 3)
            // Doorway is on the south edge of its tile
            if (tile.y < target.tile.y) {
                message("You unlock the Lighthouse front door.")
                walkOverDelay(target.tile)
            } else {
                walkOverDelay(target.tile.addY(-1))
            }
        }
    }
}
