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

on<Registered>({ isCrossbow(it.equipped(EquipSlot.Weapon)) }) { player: Player ->
    updateAttackRange(player, player.equipped(EquipSlot.Weapon))
}

on<ItemChanged>({ container == "worn_equipment" && index == EquipSlot.Weapon.index && isCrossbow(item) }) { player: Player ->
    updateAttackRange(player, item)
}

fun updateAttackRange(player: Player, weapon: Item) {
    player["attack_range"] = weapon.def.getOrNull("attack_range") as? Int ?: 7
    player.weapon = weapon
}

fun isCrossbow(item: Item) = item.name.endsWith("crossbow")

on<CombatSwing>({ player -> !swung() && isCrossbow(player.weapon) }, Priority.LOW) { player: Player ->
    val ammo = player.ammo
    if (ammo.endsWith("brutal")) {
        player.setGraphic("brutal_shoot")
    }
    player.setAnimation("crossbow_shoot")
    val bolt = when {
        ammo == "barbed_bolts" || ammo == "bone_bolts" -> ammo
        ammo.endsWith("brutal") -> "brutal_bolt"
        else -> "crossbow_bolt"
    }
    player.shoot(name = bolt, target = target, delay = 40, height = 43, endHeight = target.height, curve = 8)
    player.hit(target)
    val speed = player.weapon.def.getOrNull("attack_speed") as? Int ?: 4
    delay = if (player.attackType == "rapid") speed - 1 else speed
}