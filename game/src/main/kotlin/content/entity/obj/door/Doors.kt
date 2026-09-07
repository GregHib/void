package content.entity.obj.door

import content.area.wilderness.daemonheim.DungeoneeringParty.Companion.inDungeoneering
import content.entity.obj.ObjectTeleports
import world.gregs.voidps.engine.Script

class Doors(val teleports: ObjectTeleports) : Script {

    init {
        objectOperate("Close") { (target) ->
            closeDoor(target)
        }

        objectOperate("Open") { (target) ->
            if (inDungeoneering) {
                return@objectOperate
            }
            if (teleports.contains(target.id, target.tile, "Open")) {
                return@objectOperate
            }
            openDoor(target)
        }
    }
}
