package content.skill

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

class Skillcapes : Script {
    init {
        // Skillcapes sold through shops come with a free hood, like the dialogue-sold capes.
        bought("ranged_cape,ranged_cape_t,firemaking_cape,firemaking_cape_t,fletching_cape,fletching_cape_t,runecrafting_cape,runecrafting_cape_t,thieving_cape,thieving_cape_t,magic_cape,magic_cape_t") { item ->
            inventory.add("${item.id.removeSuffix("_t").removeSuffix("_cape")}_hood")
            jingle("buy_skillcape")
        }
    }
}
