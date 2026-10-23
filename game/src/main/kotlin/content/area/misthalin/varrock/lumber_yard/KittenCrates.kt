package content.area.misthalin.varrock.lumber_yard

import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

class KittenCrates : Script {
    init {
        objectOperate("Search", "gertrude_crate_empty") {
            message("You search the crate.")
            pause(1)
            message("You find nothing.")
        }

        npcOperate("Search", "") {
            if (get("three_little_kittens_found", false)) {
                message("You search the crate.")
                pause(1)
                message("You find nothing.")
                return@npcOperate
            }
            if (!inventory.add("three_little_kittens")) {
                message("<red>You find three little kittens but you have no space for them in your backpack.") // TODO proper message
                return@npcOperate
            }
            set("three_little_kittens_found", true)
            statement("You find three little kittens! You carefully place them in your backpack. This explains why Fluffs is so agitated.")
        }
    }
}
