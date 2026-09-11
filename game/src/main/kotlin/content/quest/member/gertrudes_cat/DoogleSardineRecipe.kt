package content.quest.member.gertrudes_cat

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class DoogleSardineRecipe : Script {
    init {
        /**
         * Source: https://runescape.wiki/w/Transcript:Gertrude%27s_Cat
         * "Use doogle leaves, raw sardine or sardine on Fluffs"
         * Implies the recipe also permits cooked sardine
         */
        itemOnItem("doogle_leaves", "raw_sardine") { _, _ ->
            inventory.remove("doogle_leaves")
            inventory.remove("raw_sardine")
            message("You rub the doogle leaves over the sardine.")
            inventory.add("doogle_sardine")
        }
        itemOnItem("doogle_leaves", "sardine") { _, _ ->
            inventory.remove("doogle_leaves")
            inventory.remove("sardine")
            message("You rub the doogle leaves over the sardine.")
            inventory.add("doogle_sardine")
        }
    }
}