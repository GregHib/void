package content.area.karamja.shilo_village

import content.entity.obj.door.enterDoor
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.type.Tile

class ShiloVillageGates : Script {
    init {
        worldSpawn {
            // TODO remove once the Shilo Village quest is implemented, the cart blocks the village until it's completed
            val cart = GameObjects.findOrNull(Tile(2877, 2951), "broken_cart_shilo_village") ?: return@worldSpawn
            GameObjects.remove(cart)
        }

        objectOperate("Open", "wooden_gate_closed,wooden_gate_2_closed") { (target) ->
            enterDoor(target)
        }
    }
}
