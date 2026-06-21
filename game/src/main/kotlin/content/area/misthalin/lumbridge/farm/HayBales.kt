package content.area.misthalin.lumbridge.farm

import content.entity.combat.hit.damage
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.random

class HayBales : Script {
    // https://x.com/JagexAsh/status/1056603985342275585

    init {
        objectOperate("Search", "hay_bales*,hay_bale_*") { (target) ->
            anim("climb_down")
            // both rs3 and osrs has these messages.
            if (target.id.contains("hay_bale_")) {
                message("You search the hay bale...")
            } else {
                message("You search the hay bales...")
            }
            delay(2)
            val roll = random.nextInt(100)
            when {
                roll < 2 -> {
                    damage(10)
                    player<Sad>("Ow! There's something sharp in there!")
                }
                roll < 12 -> {
                    if (inventory.isFull()) {
                        FloorItems.add(tile, "needle", disappearTicks = 200, owner = this)
                    } else {
                        inventory.add("needle")
                    }
                    player<Happy>("Wow! A needle!<br> Now what are the chances of finding that?")
                }
                else -> {
                    message("You find nothing of interest.")
                }
            }
        }
    }
}
