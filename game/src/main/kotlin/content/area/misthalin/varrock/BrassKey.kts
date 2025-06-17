package content.area.misthalin.varrock

import content.entity.obj.door.enterDoor
import content.entity.sound.sound
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inv.inventory

objectOperate("Open", "edgeville_dungeon_door_closed") {
    if (player.inventory.contains("brass_key")) {
        player.sound("unlock")
        enterDoor(target)
    } else {
        player.sound("locked")
        player.message("The door is locked. You need a brass key to open it.")
    }
}
