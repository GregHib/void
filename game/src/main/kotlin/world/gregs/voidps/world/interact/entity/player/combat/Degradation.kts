package world.gregs.voidps.world.interact.entity.player.combat

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.*
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.charge
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.hit.combatAttack
import world.gregs.voidps.world.interact.entity.combat.hit.combatHit

combatHit { player ->
    degrade(player)
}

combatAttack { player ->
    degrade(player)
}

val slots = arrayOf(
    EquipSlot.Hat.index,
    EquipSlot.Weapon.index,
    EquipSlot.Chest.index,
    EquipSlot.Shield.index,
    EquipSlot.Legs.index
)

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

val itemDefinitions: ItemDefinitions by inject()

itemChange { player ->
    if (!item.def.contains("charges") || item.charges != 0) {
        return@itemChange
    }
    val replacement: String = item.def.getOrNull("degrade") ?: return@itemChange
    player.softQueue("degrade") {
        val inventory = player.inventories.inventory(inventory)
        val success = if (replacement == "destroy") {
            inventory.remove(index, item.id)
        } else {
            val definition = itemDefinitions.get(replacement)
            val charges = definition["charges_start", definition["charges", 0]]
            inventory.transaction {
                replace(index, item.id, replacement, charges)
            }
        }
        if (!success) {
            return@softQueue
        }
        val message: String = item.def.getOrNull("degrade_message") ?: return@softQueue
        player.message(message)
    }
}