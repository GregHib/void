package content.area.asgarnia.rimmington

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

class RimmingtonSpade : Script {

    init {
        objectOperate("Take", "rimmington_spade") { (target) ->
            if (target.tile != Tile(2979, 3241)) {
                return@objectOperate
            }
            arriveDelay()
            if (tile == target.tile && visuals.moved) {
                walkToDelay(steps.previous)
            }
            if (inventory.isFull()) {
                message("You haven't got room to hold that.")
                return@objectOperate
            }
            if (tile != target.tile) {
                face(target.tile)
            }
            anim("take")
            if (inventory.add("spade")) {
                GameObjects.remove(target, ticks = 75)
                sound("take_item")
                message("You take the spade.")
            }
        }
    }
}
