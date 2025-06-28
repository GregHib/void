package content.area.asgarnia.taverley


import content.entity.obj.door.enterDoor
import content.entity.sound.sound
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inv.inventory

objectOperate("Open", "gate_63_closed") {
    if (player.inventory.contains("dusty_key")) {
        player.sound("unlock")
        enterDoor(target)
    } else {
        player.sound("locked")
        player.message("The gate is locked.")
    }
}