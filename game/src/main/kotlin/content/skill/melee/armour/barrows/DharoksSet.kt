package content.skill.melee.armour.barrows

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.itemAdded
import world.gregs.voidps.engine.inv.itemRemoved

class DharoksSet : Script {

    init {
        playerSpawn { player ->
            if (player.hasFullSet()) {
                player["dharoks_set_effect"] = true
            }
        }

        itemRemoved("dharoks_*", BarrowsArmour.slots, "worn_equipment") { player ->
            player.clear("dharoks_set_effect")
        }

        itemAdded("dharoks_*", BarrowsArmour.slots, "worn_equipment") { player ->
            if (player.hasFullSet()) {
                player["dharoks_set_effect"] = true
            }
        }
    }

    fun Player.hasFullSet() = BarrowsArmour.hasSet(
        this,
        "dharoks_greataxe",
        "dharoks_helm",
        "dharoks_platebody",
        "dharoks_platelegs",
    )
}
