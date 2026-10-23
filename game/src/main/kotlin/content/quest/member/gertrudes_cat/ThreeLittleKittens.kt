package content.quest.member.gertrudes_cat

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Tile

class ThreeLittleKittens : Script {
    init {
        itemOption("Drop", "three_little_kittens") {
            val playerLoc = this.tile
            inventory.remove("three_little_kittens")
            set("three_little_kittens_found", false)
            message("You place the kittens on the floor.")

            delay(2)
            val threeLittleKittens: NPC = NPCs.add("three_little_kittens", playerLoc)
            threeLittleKittens.walkTo(Tile(3294, 3507))
            threeLittleKittens.say("Mew!")

            delay(2)
            threeLittleKittens.despawn(0)
            message("The kittens have run off.")
            return@itemOption
        }
    }
}
