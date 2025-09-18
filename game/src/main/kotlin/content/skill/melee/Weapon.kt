package content.skill.melee

import content.skill.melee.weapon.attackRange
import content.skill.melee.weapon.weapon
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.variable.variableSet
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inv.inventoryChanged
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

@Script
class Weapon : Api {

    override fun spawn(player: Player) {
        updateWeapon(player, player.equipped(EquipSlot.Weapon))
    }

    init {
        inventoryChanged("worn_equipment", EquipSlot.Weapon) { player ->
            updateWeapon(player, item)
        }

        variableSet("autocast", to = null) { player ->
            updateWeapon(player, player.weapon)
        }

        variableSet("spell", to = null) { player ->
            updateWeapon(player, player.weapon)
        }

        variableSet("attack_style", to = "long_range") { player ->
            updateWeapon(player, player.weapon, 2)
        }

        variableSet("attack_style", from = "long_range") { player ->
            updateWeapon(player, player.weapon)
        }
    }

    fun updateWeapon(player: Player, weapon: Item, range: Int = 0) {
        player.attackRange = if (player.contains("autocast")) 8 else (weapon.def["attack_range", 1] + range).coerceAtMost(10)
        player["attack_speed"] = weapon.def["attack_speed", 4]
        player.weapon = weapon
    }
}
