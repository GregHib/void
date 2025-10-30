package content.skill.melee.weapon

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.inv.itemAdded
import world.gregs.voidps.engine.inv.itemRemoved
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class Whacking : Script {

    init {
        playerSpawn { player ->
            if (player.weapon.id == "rubber_chicken" || player.weapon.id == "easter_carrot") {
                player.options.set(5, "Whack")
            }
        }

        itemAdded("rubber_chicken", EquipSlot.Weapon, "worn_equipment") { player ->
            player.options.set(5, "Whack")
        }

        itemRemoved("rubber_chicken", EquipSlot.Weapon, "worn_equipment") { player ->
            player.options.remove("Whack")
        }

        itemAdded("easter_carrot", EquipSlot.Weapon, "worn_equipment") { player ->
            player.options.set(5, "Whack")
        }

        itemRemoved("easter_carrot", EquipSlot.Weapon, "worn_equipment") { player ->
            player.options.remove("Whack")
        }
    }
}
