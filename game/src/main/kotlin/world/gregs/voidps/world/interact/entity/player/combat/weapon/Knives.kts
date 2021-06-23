package world.gregs.voidps.world.interact.entity.player.combat.weapon

import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.contain.ItemChanged
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.proj.shoot


fun isKnife(item: Item?) = item != null && item.name.contains("_knife")

on<Registered>({ isKnife(it.equipped(EquipSlot.Weapon)) }) { player: Player ->
    updateAttackRange(player, player.equipped(EquipSlot.Weapon))
}

on<ItemChanged>({ container == "worn_equipment" && index == EquipSlot.Weapon.index && isKnife(item) }) { player: Player ->
    updateAttackRange(player, item)
}

fun updateAttackRange(player: Player, weapon: Item) {
    player["attack_range"] = 4
    player["attack_speed"] = 3
    player.weapon = weapon
}

on<CombatSwing>({ player -> !swung() && isKnife(player.weapon) }, Priority.HIGH) { player: Player ->
    val required = player["required_ammo", 1]
    val ammo = player.weapon.name
    player.ammo = ""
    removeAmmo(player, target, ammo, required)
    player.ammo = ammo
}

on<CombatSwing>({ player -> !swung() && isKnife(player.weapon) }, Priority.LOW) { player: Player ->
    val ammo = player.ammo.removeSuffix("_p++").removeSuffix("_p+").removeSuffix("_p")
    player.setAnimation("throw_projectile")
    player.setGraphic("${ammo}_throw", height = 100)
    player.shoot(name = ammo, target = target, delay = 30, height = 40, endHeight = target.height, curve = 8)
    player.hit(target, delay = if (player.attackType == "rapid") 1 else 2)
    delay = player["attack_speed", 4] - if (player.attackType == "rapid") 1 else 0
}