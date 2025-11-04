package content.skill.melee.armour

import content.entity.combat.hit.combatAttack
import content.entity.combat.hit.combatDamage
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.*
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class Degradation : Script {

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

        slotChanged {
            val inventory = inventories.inventory(it.inventory)
            val degrade: String = it.fromItem.def.getOrNull("degrade") ?: return@slotChanged
            if (degrade == "destroy" && it.item.isNotEmpty()) {
                return@slotChanged
            }
            if (it.item.id != degrade) {
                return@slotChanged
            }
            if (inventory.charges(this, it.fromIndex) != 0) {
                return@slotChanged
            }
            val message: String = it.fromItem.def.getOrNull("degrade_message") ?: return@slotChanged
            message(message)
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
