package content.skill.melee

import content.skill.melee.weapon.attackRange
import content.skill.melee.weapon.weapon
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.inventoryChanged
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class Weapon : Script {

    init {
        playerSpawn { player ->
            updateWeapon(player, player.equipped(EquipSlot.Weapon))
        }

        variableSet("autocast,spell") { player, _, _, _ -> updateWeapon(player, player.weapon) }
        variableSet("attack_style") { player, _, from, to ->
            if (to == "long_range") {
                updateWeapon(player, player.weapon, 2)
            } else if (from == "long_range") {
                updateWeapon(player, player.weapon)
            }
        }

        inventoryChanged("worn_equipment", EquipSlot.Weapon) { player ->
            updateWeapon(player, item)
        }
    }

    fun updateWeapon(player: Player, weapon: Item, range: Int = 0) {
        player.attackRange = if (player.contains("autocast")) 8 else (weapon.def["attack_range", 1] + range).coerceAtMost(10)
        player["attack_speed"] = weapon.def["attack_speed", 4]
        player.weapon = weapon
    }
}
