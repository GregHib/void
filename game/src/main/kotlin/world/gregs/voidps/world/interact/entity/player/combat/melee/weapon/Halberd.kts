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

fun isHalberd(item: Item?) = item != null && item.name.endsWith("halberd")

on<Registered>({ isHalberd(it.equipped(EquipSlot.Weapon)) }) { player: Player ->
    updateWeapon(player, player.equipped(EquipSlot.Weapon))
}

on<ItemChanged>({ container == "worn_equipment" && index == EquipSlot.Weapon.index && isHalberd(item) }) { player: Player ->
    updateWeapon(player, item)
}

fun updateWeapon(player: Player, weapon: Item) {
    player["attack_range"] = 2
    player.weapon = weapon
}

on<CombatSwing>({ !swung() && isHalberd(it.weapon) }, Priority.LOWER) { player: Player ->
    player.setAnimation("halberd_${player.attackType}")
    player.hit(target)
    delay = 7
}

on<CombatHit>({ isHalberd(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("spear_block")
}