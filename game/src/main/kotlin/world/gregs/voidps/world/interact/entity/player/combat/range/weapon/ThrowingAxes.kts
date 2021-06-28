package world.gregs.voidps.world.interact.entity.player.combat.range.weapon

import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.contain.ItemChanged
import world.gregs.voidps.engine.entity.character.contain.equipment
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
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.proj.shoot

fun isThrowingAxe(item: Item?) = item != null && item.name.contains("_throwing_axe")

on<Registered>({ isThrowingAxe(it.equipped(EquipSlot.Weapon)) }) { player: Player ->
    updateAttackRange(player, player.equipped(EquipSlot.Weapon))
}

on<ItemChanged>({ container == "worn_equipment" && index == EquipSlot.Weapon.index && isThrowingAxe(item) }) { player: Player ->
    updateAttackRange(player, item)
}

fun updateAttackRange(player: Player, weapon: Item) {
    player["attack_range"] = 4
    player["attack_speed"] = 5
    player.weapon = weapon
}

on<CombatSwing>({ player -> !swung() && isThrowingAxe(player.weapon) }, Priority.HIGH) { player: Player ->
    val required = player["required_ammo", 1]
    val ammo = player.weapon.name
    player.ammo = ""
    if (!player.equipment.remove(ammo, required)) {
        player.message("That was your last one!")
        delay = -1
        return@on
    }
    player.ammo = ammo
}

on<CombatSwing>({ player -> !swung() && isThrowingAxe(player.weapon) }, Priority.LOW) { player: Player ->
    val ammo = player.ammo.removePrefix("corrupt_")
    player.setAnimation(if (ammo.contains("morrigans")) "throw_javelin" else "throw_projectile")
    player.setGraphic("${ammo}_throw", height = 100)
    player.shoot(name = ammo, target = target, delay = 40, height = if (ammo.contains("morrigans")) 50 else 43, endHeight = target.height, curve = 8)
    player.hit(target)
    delay = player["attack_speed", 4] - if (player.attackType == "rapid") 1 else 0
}