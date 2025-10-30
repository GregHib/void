package content.area.misthalin.varrock

import content.entity.obj.door.enterDoor
import content.entity.sound.sound
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inv.inventory

class BrassKey : Script {

    init {
        objectOperate("Open", "edgeville_dungeon_door_closed") { (target) ->
            if (inventory.contains("brass_key")) {
                sound("unlock")
                enterDoor(target)
            } else {
                sound("locked")
                message("The door is locked. You need a brass key to open it.")
            }
        }
    }
}
