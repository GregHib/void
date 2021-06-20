package world.gregs.voidps.world.interact.entity.player.combat.weapon

import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.contain.ItemChanged
import world.gregs.voidps.engine.entity.character.contain.equipment
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
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

fun isJavelin(item: Item?) = item != null && item.name.contains("_javelin")

on<Registered>({ isJavelin(it.equipped(EquipSlot.Weapon)) }) { player: Player ->
    updateAttackRange(player, player.equipped(EquipSlot.Weapon))
}

on<ItemChanged>({ container == "worn_equipment" && index == EquipSlot.Weapon.index && isJavelin(item) }) { player: Player ->
    updateAttackRange(player, item)
}

fun updateAttackRange(player: Player, weapon: Item) {
    player["attack_range"] = 5
    player["attack_speed"] = 6
    player.weapon = weapon
}

on<CombatSwing>({ player -> !swung() && isJavelin(player.weapon) }, Priority.HIGH) { player: Player ->
    val required = player["required_ammo", 1]
    val ammo = player.weapon.name
    player.ammo = ""
    if (!player.equipment.remove(ammo, required)) {
        player.message("That was your last one!")
        delay = -1
        return@on
    }
    player.ammo = ammo.removePrefix("corrupt_").removeSuffix("_p++").removeSuffix("_p+").removeSuffix("_p")
}

on<CombatSwing>({ player -> !swung() && isJavelin(player.weapon) }, Priority.LOW) { player: Player ->
    val ammo = player.ammo
    player.setAnimation("throw_javelin")
    player.shoot(name = ammo, target = target, delay = 40, height = 45, endHeight = target.height, curve = 8)
    player.hit(target)
    delay = player["attack_speed", 4] - if (player.attackType == "rapid") 1 else 0
}