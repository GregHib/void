package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.contain.ItemChanged
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*

fun isWarSpear(item: Item?) = item != null && item.name.startsWith("guthans_warspear")

on<Registered>({ isWarSpear(it.equipped(EquipSlot.Weapon)) }) { player: Player ->
    updateWeapon(player, player.equipped(EquipSlot.Weapon))
}

on<ItemChanged>({ container == "worn_equipment" && index == EquipSlot.Weapon.index && isWarSpear(item) }) { player: Player ->
    updateWeapon(player, item)
}

fun updateWeapon(player: Player, weapon: Item) {
    player["attack_range"] = 1
    player.weapon = weapon
}

on<CombatSwing>({ !swung() && isWarSpear(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("guthans_spear_${
        when (player.attackType) {
            "swipe" -> "swipe"
            else -> "attack"
        }
    }")
    player.hit(target)
    delay = 5
}

on<CombatHit>({ isWarSpear(it.weapon) }) { player: Player ->
    player.setAnimation("guthans_spear_block")
}