package content.entity.player.inv.item.drop

import content.entity.player.inv.item.tradeable
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class ItemPlace(val floorItems: FloorItems) : Script {

    init {
        itemOnObjectOperate(obj = "table*") { (target, item, slot) ->
            if (!World.members && item.def["members", false]) {
                message("To use this item please login to a members' server.")
                return@itemOnObjectOperate
            }
            if (!item.tradeable) {
                message("You cannot put that on a table.")
                return@itemOnObjectOperate
            }
            if (inventory.remove(slot, item.id, item.amount)) {
                anim("take")
                sound("drop_item")
                val tile = target.nearestTo(tile)
                floorItems.add(tile, item.id, item.amount, revealTicks = 100, disappearTicks = 1000, owner = this)
            }
        }
    }
}
