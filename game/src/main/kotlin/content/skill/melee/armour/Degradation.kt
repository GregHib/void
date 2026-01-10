package content.skill.melee.armour

import content.entity.combat.inCombat
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.*
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

/**
 * Tracks and deducts charges from degradable-items.
 *
 * Item charges can be depleted by:
 *  - combat: per tick in combat
 *  - equip: per tick when worn
 *  - per_hit: per enemy hit
 *  - per_attack: per hit dealt to an enemy
 *  - teleport: per item use
 *
 * Technically, this should be:
 * - 100-tick degrade cycle for ancient equipment
 * - combat hits with a 90-tick cooldown for barrows equipment
 *
 * However, as osrs found (and changed), this allows exploitation and issues where equipment can last
 * a lot longer or shorter than expected, so a more generic per-tick solution was chosen instead.
 */
class Degradation : Script {

    val slots = arrayOf(
        EquipSlot.Hat.index,
        EquipSlot.Weapon.index,
        EquipSlot.Chest.index,
        EquipSlot.Shield.index,
        EquipSlot.Legs.index,
    )

    init {
        playerSpawn {
            for (slot in slots) {
                val deplete: String = equipment.getOrNull(slot)?.def?.getOrNull("deplete") ?: continue
                if (deplete == "combat" || deplete == "equip") {
                    softTimers.start("degrading")
                }
            }
        }

        timerStart("degrading") { 1 }

        timerTick("degrading") {
            degrade(this)
        }

        slotChanged {
            val deplete: String = it.item.def.getOrNull("deplete") ?: return@slotChanged
            if (deplete == "combat" || deplete == "equip") {
                softTimers.start("degrading")
            }
            degradeMessage(it)
        }

        combatDamage {
            for (slot in slots) {
                val deplete: String = equipment.getOrNull(slot)?.def?.getOrNull("deplete") ?: continue
                if (deplete == "per_hit") {
                    equipment.discharge(this, slot)
                }
            }
        }

        combatAttack {
            val deplete: String = it.weapon.def.getOrNull("deplete") ?: return@combatAttack
            if (deplete == "per_attack") {
                equipment.discharge(this, EquipSlot.Weapon.index)
            }
        }
    }

    fun degrade(player: Player): Int {
        var found = false
        val inventory = player.equipment
        for (slot in slots) {
            val deplete: String = inventory.getOrNull(slot)?.def?.getOrNull("deplete") ?: continue
            if (deplete == "combat" && player.inCombat) {
                inventory.discharge(player, slot)
                found = true
            } else if (deplete == "equip") {
                inventory.discharge(player, slot)
                found = true
            }
        }
        return if (found) Timer.CONTINUE else Timer.CANCEL
    }

    fun Player.degradeMessage(changed: InventorySlotChanged) {
        val inventory = inventories.inventory(changed.inventory)
        val degrade: String = changed.fromItem.def.getOrNull("degrade") ?: return
        if (degrade == "destroy" && changed.item.isNotEmpty()) {
            return
        }
        if (changed.item.id != degrade) {
            return
        }
        if (inventory.charges(this, changed.fromIndex) != 0) {
            return
        }
        val message: String = changed.fromItem.def.getOrNull("degrade_message") ?: return
        message(message)
    }
}
