package world.gregs.voidps.world.interact.entity.player.combat.weapon

import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.contain.ItemChanged
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.proj.shoot

on<Registered>({ isBow(it.equipped(EquipSlot.Weapon)) }) { player: Player ->
    updateWeapon(player, player.equipped(EquipSlot.Weapon))
}

on<ItemChanged>({ container == "worn_equipment" && index == EquipSlot.Weapon.index && isBow(item) }) { player: Player ->
    updateWeapon(player, item)
}

fun updateWeapon(player: Player, weapon: Item) {
    player["attack_range"] = weapon.def.getOrNull("attack_range") as? Int ?: 7
    player.weapon = weapon
}

fun isBow(item: Item) = item.name.endsWith("bow") && !item.name.endsWith("crossbow")

on<CombatSwing>({ player -> !swung() && isBow(player.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("bow_shoot")
    val ammo = player.ammo
    player.setGraphic("${ammo}_shoot", height = 100)
    player.shoot(name = ammo, target = target, delay = 40, height = 43, endHeight = target.height, curve = 8)
    player.hit(target)
    val speed = player.weapon.def["attack_speed", 4]
    delay = if (player.attackType == "rapid") speed - 1 else speed
}