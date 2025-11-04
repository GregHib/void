package content.skill.melee.armour.barrows

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.ItemAdded
import world.gregs.voidps.engine.inv.ItemRemoved

class VeracsSet : Script {

    init {
        playerSpawn {
            if (hasFullSet()) {
                set("veracs_set_effect", true)
            }
        }

        for (slot in BarrowsArmour.slots) {
            itemAdded("veracs_*", "worn_equipment", slot, ::added)
            itemRemoved("veracs_*", "worn_equipment", slot, ::removed)
        }
    }

    fun added(player: Player, update: ItemAdded) {
        if (player.hasFullSet()) {
            player["veracs_set_effect"] = true
        }
    }

    fun removed(player: Player, update: ItemRemoved) {
        player.clear("veracs_set_effect")
    }

    fun Player.hasFullSet() = BarrowsArmour.hasSet(
        this,
        "veracs_flail",
        "veracs_helm",
        "veracs_brassard",
        "veracs_plateskirt",
    )
}
