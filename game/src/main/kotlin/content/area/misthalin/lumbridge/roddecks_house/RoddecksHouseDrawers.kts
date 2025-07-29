package content.area.misthalin.lumbridge.roddecks_house

import content.entity.sound.areaSound
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

objectOperate("Open", "lumbridge_drawers_closed") {
    player.anim("open_chest")
    areaSound("drawer_open", target.tile)
    target.replace(target.id.replace("_closed", "_opened"), ticks = TimeUnit.MINUTES.toTicks(3))
}

objectOperate("Close", "lumbridge_drawers_opened") {
    player.anim("close_chest")
    areaSound("drawer_close", target.tile)
    target.replace(target.id.replace("_opened", "_closed"), ticks = TimeUnit.MINUTES.toTicks(3))
}

objectOperate("Search", "lumbridge_drawers_opened") {
    player.message("Nothing terribly interesting in here.")
}
