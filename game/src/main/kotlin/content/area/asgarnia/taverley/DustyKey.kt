package content.area.asgarnia.taverley

import content.entity.obj.door.enterDoor
import content.entity.sound.sound
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.inv.inventory

class DustyKey : Script {

    init {
        objectOperate("Open", "gate_63_closed") { (target) ->
            if (inventory.contains("dusty_key")) {
                sound("unlock")
                enterDoor(target)
            } else {
                sound("locked")
                message("The gate is locked.")
            }
        }
    }
}
