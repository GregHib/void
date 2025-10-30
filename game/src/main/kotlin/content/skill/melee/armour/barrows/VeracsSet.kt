package content.skill.melee.armour.barrows

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.itemAdded
import world.gregs.voidps.engine.inv.itemRemoved

class VeracsSet : Script {

    init {
        playerSpawn {
            if (hasFullSet()) {
                set("veracs_set_effect", true)
            }
        }

        itemRemoved("veracs_*", BarrowsArmour.slots, "worn_equipment") { player ->
            player.clear("veracs_set_effect")
        }

        itemAdded("veracs_*", BarrowsArmour.slots, "worn_equipment") { player ->
            if (player.hasFullSet()) {
                player["veracs_set_effect"] = true
            }
        }
    }

    fun Player.hasFullSet() = BarrowsArmour.hasSet(
        this,
        "veracs_flail",
        "veracs_helm",
        "veracs_brassard",
        "veracs_plateskirt",
    )
}
