package content.entity.player.inv.item.drop

import content.entity.player.inv.item.tradeable
import content.entity.sound.sound
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnObjectOperate
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class ItemPlace : Script {

    val floorItems: FloorItems by inject()

    init {
        itemOnObjectOperate(obj = "table*") {
            if (!World.members && item.def["members", false]) {
                player.message("To use this item please login to a members' server.")
                return@itemOnObjectOperate
            }
            if (!item.tradeable) {
                player.message("You cannot put that on a table.")
                return@itemOnObjectOperate
            }
            if (player.inventory.remove(itemSlot, item.id, item.amount)) {
                player.anim("take")
                player.sound("drop_item")
                val tile = target.nearestTo(player.tile)
                floorItems.add(tile, item.id, item.amount, revealTicks = 100, disappearTicks = 1000, owner = player)
            }
        }
    }
}
