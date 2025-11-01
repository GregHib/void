package content.area.misthalin.lumbridge.roddecks_house

import content.entity.sound.areaSound
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

class RoddecksHouseDrawers : Script {

    init {
        objectOperate("Open", "lumbridge_drawers_closed") { (target) ->
            anim("open_chest")
            areaSound("drawer_open", target.tile)
            target.replace(target.id.replace("_closed", "_opened"), ticks = TimeUnit.MINUTES.toTicks(3))
        }

        objectOperate("Close", "lumbridge_drawers_opened") { (target) ->
            anim("close_chest")
            areaSound("drawer_close", target.tile)
            target.replace(target.id.replace("_opened", "_closed"), ticks = TimeUnit.MINUTES.toTicks(3))
        }

        objectOperate("Search", "lumbridge_drawers_opened") {
            message("Nothing terribly interesting in here.")
        }
    }
}
