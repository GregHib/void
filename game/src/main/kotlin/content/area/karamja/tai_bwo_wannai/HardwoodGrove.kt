package content.area.karamja.tai_bwo_wannai

import content.entity.obj.door.enterDoor
import content.entity.player.dialogue.type.choice
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class HardwoodGrove : Script {

    val entryFee = 100

    init {
        objectOperate("Open", "hardwood_grove_doors_closed,hardwood_grove_doors_2_closed") { (target) ->
            if (tile.x >= target.tile.x) {
                enterDoor(target)
                return@objectOperate
            }
            choice("Pay $entryFee trading sticks to enter?") {
                option("Yes") {
                    payAndEnter(target)
                }
                option("No")
            }
        }

        objectOperate("Quick-pay(100)", "hardwood_grove_doors_closed,hardwood_grove_doors_2_closed") { (target) ->
            if (tile.x >= target.tile.x) {
                enterDoor(target)
                return@objectOperate
            }
            payAndEnter(target)
        }
    }

    private suspend fun Player.payAndEnter(door: GameObject) {
        if (!inventory.remove("trading_sticks", entryFee)) {
            message("You need $entryFee trading sticks to use this door.")
            return
        }
        enterDoor(door)
    }
}
