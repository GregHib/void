package content.skill.melee

import content.skill.melee.weapon.attackRange
import content.skill.melee.weapon.weapon
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.variable.Variable
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

    @Variable("autocast,spell,attack_style")
    override fun variableSet(player: Player, key: String, from: Any?, to: Any?) {
        if (key == "autocast" && to == null) {
            updateWeapon(player, player.weapon)
        } else if (key == "spell" && to == null) {
            updateWeapon(player, player.weapon)
        } else if (key == "attack_style") {
            if (to == "long_range") {
                updateWeapon(player, player.weapon, 2)
            } else if (from == "long_range") {
                updateWeapon(player, player.weapon)
            }
        }
    }

    init {
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
