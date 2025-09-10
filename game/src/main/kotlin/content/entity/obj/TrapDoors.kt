package content.entity.obj

import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

@Script
class TrapDoors {

    init {
        objectOperate("Open", "trapdoor_*_closed") {
            player.anim("open_chest")
            target.replace(target.id.replace("_closed", "_opened"), ticks = TimeUnit.MINUTES.toTicks(3))
        }

        objectOperate("Close", "trapdoor_*_opened") {
            player.anim("close_chest")
            target.replace(target.id.replace("_opened", "_closed"), ticks = TimeUnit.MINUTES.toTicks(3))
        }
    }
}
