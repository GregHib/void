package content.entity.obj

import content.area.misthalin.ham_hideout.HamHideout.Companion.handleLockedTrapdoorOpen
import content.area.misthalin.ham_hideout.HamHideout.Companion.handlePrivateTrapdoorClose
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

class TrapDoors : Script {

    init {
        objectOperate("Open", "trapdoor_*_closed") { (target) ->
            if (handleLockedTrapdoorOpen(this, target)) {
                return@objectOperate
            }
            anim("open_chest")
            if (target.def.transforms != null) {
                return@objectOperate
            }
            target.replace(target.id.replace("_closed", "_opened"), ticks = TimeUnit.MINUTES.toTicks(3))
        }

        objectOperate("Close", "trapdoor_*_opened") { (target) ->
            if (handlePrivateTrapdoorClose(this, target)) {
                return@objectOperate
            }
            anim("close_chest")
            if (target.def.transforms != null) {
                return@objectOperate
            }
            target.replace(target.id.replace("_opened", "_closed"), ticks = TimeUnit.MINUTES.toTicks(3))
        }
    }
}
