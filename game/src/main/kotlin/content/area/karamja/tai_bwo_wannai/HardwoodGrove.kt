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

    val doors = "hardwood_grove_doors_closed,hardwood_grove_doors_2_closed"
    val entryFee = 100

    init {
        objectOperate("Open", doors) { (target) ->
            enterGrove(target, quickPay = false)
        }

        objectOperate("Quick-pay(100)", doors) { (target) ->
            enterGrove(target, quickPay = true)
        }
    }

    private suspend fun Player.enterGrove(door: GameObject, quickPay: Boolean) {
        // The doors are on the west edge of their tiles, leaving the grove is free
        if (tile.x >= door.tile.x) {
            enterDoor(door)
            return
        }
        if (quickPay) {
            payAndEnter(door)
            return
        }
        choice("Pay $entryFee trading sticks to enter?") {
            option("Yes") {
                payAndEnter(door)
            }
            option("No")
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
