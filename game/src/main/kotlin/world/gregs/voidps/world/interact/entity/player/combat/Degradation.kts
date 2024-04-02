package world.gregs.voidps.world.interact.entity.player.combat

import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.hit.combatAttack
import world.gregs.voidps.world.interact.entity.combat.hit.combatHit
import world.gregs.voidps.world.interact.entity.player.effect.degrade.Degrade

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
        Degrade.discharge(player, inventory, slot, 1)
    }
}