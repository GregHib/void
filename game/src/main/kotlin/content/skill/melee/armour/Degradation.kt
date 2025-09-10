package content.skill.melee.armour

import content.entity.combat.hit.combatAttack
import content.entity.combat.hit.combatDamage
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inv.*
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

@Script
class Degradation {

    val slots = arrayOf(
        EquipSlot.Hat.index,
        EquipSlot.Weapon.index,
        EquipSlot.Chest.index,
        EquipSlot.Shield.index,
        EquipSlot.Legs.index,
    )

    init {
        combatDamage { player ->
            degrade(player)
        }

        combatAttack { player ->
            degrade(player)
        }

        inventoryChanged { player ->
            val inventory = player.inventories.inventory(inventory)
            val degrade: String = fromItem.def.getOrNull("degrade") ?: return@inventoryChanged
            if (degrade == "destroy" && item.isNotEmpty()) {
                return@inventoryChanged
            }
            if (item.id != degrade) {
                return@inventoryChanged
            }
            if (inventory.charges(player, fromIndex) != 0) {
                return@inventoryChanged
            }
            val message: String = fromItem.def.getOrNull("degrade_message") ?: return@inventoryChanged
            player.message(message)
        }
    }

    fun degrade(player: Player) {
        if (player.hasClock("degraded")) {
            return
        }
        player.start("degraded", 1)
        val inventory = player.equipment
        for (slot in slots) {
            val equipment = inventory.getOrNull(slot) ?: continue
            val deplete: String = equipment.def.getOrNull("deplete") ?: continue
            if (deplete != "combat") {
                continue
            }
            inventory.discharge(player, slot)
        }
    }
}
