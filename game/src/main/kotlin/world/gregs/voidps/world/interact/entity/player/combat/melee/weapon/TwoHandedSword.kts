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

fun is2hSword(item: Item?) = item != null && (item.name.endsWith("2h_sword") || item.name == "shadow_sword" || isFunWeapon(item))
fun isFunWeapon(item: Item) = item.name == "giants_hand" || item.name == "spatula"

on<Registered>({ is2hSword(it.equipped(EquipSlot.Weapon)) }) { player: Player ->
    updateWeapon(player, player.equipped(EquipSlot.Weapon))
}

on<ItemChanged>({ container == "worn_equipment" && index == EquipSlot.Weapon.index && is2hSword(item) }) { player: Player ->
    updateWeapon(player, item)
}

fun updateWeapon(player: Player, weapon: Item) {
    player["attack_range"] = 1
    player.weapon = weapon
}

on<CombatSwing>({ !swung() && is2hSword(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("2h_sword_${player.attackType}")
    player.hit(target)
    delay = 7
}

on<CombatHit>({ is2hSword(it.weapon) }, Priority.LOWER) { player: Player ->
    player.setAnimation("2h_sword_hit", override = true)
}