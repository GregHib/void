package content.quest.member.gertrudes_cat

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Tile

private const val THREE_LITTLE_KITTENS_STRING_NAME = "three_little_kittens"

class ThreeLittleKittens : Script {
    init {
        // This works, but if the player moves then the kittens aren't shown properly running away.
        // Putting it in a queue does not fix it.
        // If you drop the kittens up the ladder by Fluffs then the kittens will stick around. Or perhaps I delayed in doing something so the kittens spawned in, but performed the action before they despawned.
        itemOption("Drop", THREE_LITTLE_KITTENS_STRING_NAME) {
            val playerLoc = this.tile
            inventory.remove(THREE_LITTLE_KITTENS_STRING_NAME)
            set(KITTENS_FOUND, false)
            message("You place the kittens on the floor.")

            pause(2)
            val threeLittleKittens: NPC = NPCs.add(THREE_LITTLE_KITTENS_STRING_NAME, playerLoc)
            threeLittleKittens.walkTo(Tile(3294, 3507))
            threeLittleKittens.say("Mew!")

            pause(2)
            threeLittleKittens.despawn(0)
            message("The kittens have run off.")
            return@itemOption
        }
    }
}
