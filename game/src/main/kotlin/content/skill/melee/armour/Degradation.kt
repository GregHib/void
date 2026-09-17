package content.skill.melee.armour

import content.entity.combat.inCombat
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.InventorySlotChanged
import world.gregs.voidps.engine.inv.charges
import world.gregs.voidps.engine.inv.discharge
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

/**
 * Tracks and deducts charges from degradable items.
 *
 * Item charges can be depleted by:
 *  - combat: once per degradation interval while in combat
 *  - equip: once per degradation interval while worn
 *  - per_hit: per enemy hit
 *  - per_attack: per hit dealt to an enemy
 *  - teleport: per item use
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
            startDegradationTimer()
        }

        combatStart {
            startDegradationTimer()
        }

        timerStart("degrading") { INITIAL_DEGRADATION_DELAY }

        timerTick("degrading") {
            if (degrade(this) == Timer.CANCEL) {
                Timer.CANCEL
            } else {
                DEGRADATION_INTERVAL
            }
        }

        slotChanged {
            val deplete: String = it.item.def.getOrNull("deplete") ?: ""
            if (deplete == "combat" || deplete == "equip") {
                startDegradationTimer()
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

    private fun Player.startDegradationTimer() {
        if (slots.any { slot ->
                val deplete = equipment.getOrNull(slot)?.def?.getOrNull<String>("deplete")
                deplete == "combat" || deplete == "equip"
            }
        ) {
            softTimers.start("degrading")
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
        val degrade: String = changed.fromItem.def.getOrNull("degrade") ?: return
        if (degrade == "destroy") {
            if (changed.item.isNotEmpty()) {
                return
            }
        } else if (changed.item.id != degrade) {
            return
        }

        val message = changed.fromItem.def.getOrNull<String>("degrade_message")
            ?: when {
                degrade == "destroy" || changed.item.isEmpty() ->
                    "Your ${changed.fromItem.def.name.lowercase()} has run out of durability and is destroyed."
                !changed.item.def.contains("degrade") ->
                    "Your ${changed.fromItem.def.name.lowercase()} has run out of durability and is now broken."
                else -> return
            }
        message(message)
    }

    companion object {
        private const val INITIAL_DEGRADATION_DELAY = 1
        private const val DEGRADATION_INTERVAL = 100
    }
}

data class ItemDurability(
    val current: Int,
    val maximum: Int,
) {
    val percent: Int
        get() = if (maximum <= 0) 0 else (current * 100L / maximum).toInt().coerceIn(0, 100)
}

fun Item.durability(player: Player): ItemDurability? {
    if (isEmpty() || !def.contains("charges") || !def.contains("degrade")) {
        return null
    }

    val configuredCharges = def.getOrNull<Int>("charges") ?: return null
    if (configuredCharges > 1) {
        return ItemDurability(charges(player), configuredCharges)
    }

    val maximum = def.getOrNull<Int>("charges_max")
        ?: def.getOrNull<String>("degrade")
            ?.let { ItemDefinitions.getOrNull(it)?.getOrNull<Int>("charges") }
        ?: degradationSteps(id)
    if (maximum <= 0) {
        return null
    }

    val current = if (configuredCharges == 1 && maximum > 1) maximum else charges(player)
    return ItemDurability(current.coerceIn(0, maximum), maximum)
}

fun Item.durabilityMessage(player: Player): String? {
    val durability = durability(player) ?: return null
    return "Your ${def.name.lowercase()} has ${durability.current}/${durability.maximum} charges remaining (${durability.percent}%)."
}

private fun degradationSteps(start: String): Int {
    var current = start
    var steps = 0
    val visited = mutableSetOf<String>()
    while (visited.add(current)) {
        val definition = ItemDefinitions.getOrNull(current) ?: break
        if (definition.getOrNull<Int>("charges") != 1) {
            break
        }
        steps++
        val next = definition.getOrNull<String>("degrade") ?: break
        if (next == "destroy") {
            break
        }
        current = next
    }
    return steps
}
