package content.entity.obj.door

import content.area.misthalin.ham_hideout.HamHideout.Companion.handleLockedPrisonDoorOpen
import content.area.misthalin.ham_hideout.HamHideout.Companion.handleLockedTrapdoorOpen
import content.entity.obj.ObjectTeleports
import world.gregs.voidps.engine.Script

class Doors(val teleports: ObjectTeleports) : Script {

    init {
        objectOperate("Close") { (target) ->
            closeDoor(target)
        }

        objectOperate("Open") { (target) ->
            if (handleLockedTrapdoorOpen(this, target)) {
                return@objectOperate
            }
            if (handleLockedPrisonDoorOpen(this, target)) {
                return@objectOperate
            }
            if (teleports.contains(target.id, target.tile, "Open")) {
                return@objectOperate
            }
            openDoor(target)
        }
    }
}
