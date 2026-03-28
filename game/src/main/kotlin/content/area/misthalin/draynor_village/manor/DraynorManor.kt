package content.area.misthalin.draynor_village.manor

import content.entity.obj.door.enterDoor
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.sound

class DraynorManor : Script {
    init {
        objectOperate("Open", "draynor_manor_door_sign_closed,draynor_manor_door_closed") { (target) ->
            if (tile.y >= 3354) {
                message("The doors won't open.")
                return@objectOperate
            }
            enterDoor(target)
            message("The doors slam shut behind you.")
            sound("big_door_close")
        }
    }
}
