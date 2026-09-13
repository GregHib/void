package content.quest.member.gertrudes_cat

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

private const val THREE_LITTLE_KITTENS_STRING_NAME = "three_little_kittens"

class ThreeLittleKittens : Script {
    init {
        itemOption("Drop", THREE_LITTLE_KITTENS_STRING_NAME) {
            inventory.remove(THREE_LITTLE_KITTENS_STRING_NAME)
            set(KITTENS_FOUND, false)
            message("You place the kittens on the floor.")
            pause(2)
            this.say("Mew!")
            pause(2)
            message("The kittens have run off.")
            return@itemOption
        }
    }
}