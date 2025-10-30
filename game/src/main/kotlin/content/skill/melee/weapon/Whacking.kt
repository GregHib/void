package content.skill.melee.weapon

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.inv.itemAdded
import world.gregs.voidps.engine.inv.itemRemoved
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class Whacking : Script {

    init {
        playerSpawn {
            if (weapon.id == "rubber_chicken" || weapon.id == "easter_carrot") {
                options.set(5, "Whack")
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
