package content.skill.melee.weapon

import world.gregs.voidps.engine.Script
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class Whacking : Script {

    init {
        playerSpawn {
            if (weapon.id == "rubber_chicken" || weapon.id == "easter_carrot") {
                options.set(5, "Whack")
            }
        }

        itemAdded("rubber_chicken", "worn_equipment", EquipSlot.Weapon) {
            options.set(5, "Whack")
        }

        itemRemoved("rubber_chicken", "worn_equipment", EquipSlot.Weapon) {
            options.remove("Whack")
        }

        itemAdded("easter_carrot", "worn_equipment", EquipSlot.Weapon) {
            options.set(5, "Whack")
        }

        itemRemoved("easter_carrot", "worn_equipment", EquipSlot.Weapon) {
            options.remove("Whack")
        }
    }
}
