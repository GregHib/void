package world.gregs.voidps.world.interact.entity.player.combat.armour

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.hit.combatHit
import world.gregs.voidps.world.interact.entity.combat.hit.directHit
import world.gregs.voidps.world.interact.entity.player.effect.degrade.Degrade
import world.gregs.voidps.world.interact.entity.player.equip.inventoryItem

combatHit { player ->
    if (source == player || type == "deflect" || type == "poison" || type == "disease" || type == "healed" || damage < 1) {
        return@combatHit
    }
    if (player.equipped(EquipSlot.Ring).id != "ring_of_recoil") {
        return@combatHit
    }
    if (source is NPC && source.def["immune_deflect", false]) {
        return@combatHit
    }
    val charges = Degrade.charges(player, "worn_equipment", EquipSlot.Ring.index)
    val deflect = (10 + (damage / 10)).coerceAtMost(charges)
    if (Degrade.discharge(player, "worn_equipment", EquipSlot.Ring.index, deflect)) {
        source.directHit(deflect, "deflect", source = player)
    }
}

inventoryItem("Check", "ring_of_recoil", "worn_equipment") {
    val charges = Degrade.charges(player, "worn_equipment", EquipSlot.Ring.index)
    player.message("You can inflict $charges more points of damage before a ring will shatter.")
}