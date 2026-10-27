package content.area.karamja.shilo_village

import content.entity.obj.door.enterDoor
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject

class ShiloVillageGates : Script {
    init {
        objectOperate("Look-at", "broken_cart_shilo_village") { (target) ->
            climbCart(target)
        }

        objectOperate("Search", "broken_cart_shilo_village") { (target) ->
            climbCart(target)
        }

        objectOperate("Open", "wooden_gate_closed,wooden_gate_2_closed") { (target) ->
            enterDoor(target)
        }
    }

    // TODO only allow climbing over once the Shilo Village quest is complete, "The village looks like it isn't safe to enter." otherwise
    private suspend fun Player.climbCart(cart: GameObject) {
        statement("You approach the cart and see undead creatures gathering by the village gates. There is a note attached to the cart. The note says, <red>:: Danger :: Deadly green mist DO NOT ENTER IF YOU VALUE YOUR LIFE !")
        choice {
            option("Yes, I am very nimble and agile!") {
                message("You nimbly jump from one side of the cart...")
                anim("climb_up")
                delay(2)
                tele(tile.addX(if (tile.x > cart.tile.x) -4 else 4))
                message("...to the other and climb down again.")
            }
            option("No, I am happy where I am thanks!") {
                message("You think better of clambering over the cart, you might get dirty.")
                delay(2)
                say("I'd probably have just scraped my knees up as well.")
            }
        }
    }
}
