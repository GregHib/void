package content.entity.obj.door

import content.entity.obj.ObjectTeleports
import world.gregs.voidps.engine.Script

class Doors(val teleports: ObjectTeleports) : Script {

    init {
        objectOperate("Close") { (target) ->
            closeDoor(target)
        }

        objectOperate("Open") { (target) ->
            if (teleports.teleport(this, target, "Open")) {
                return@objectOperate
            }
            openDoor(target)
        }
    }
}
