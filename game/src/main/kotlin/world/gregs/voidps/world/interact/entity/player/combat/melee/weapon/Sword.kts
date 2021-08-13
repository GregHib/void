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

fun isSword(item: Item?) = item != null && (item.name.endsWith("sword") || item.name.endsWith("rapier"))

on<Registered>({ isSword(it.equipped(EquipSlot.Weapon)) }) { player: Player ->
    updateWeapon(player, player.equipped(EquipSlot.Weapon))
}

on<ItemChanged>({ container == "worn_equipment" && index == EquipSlot.Weapon.index && isSword(item) }) { player: Player ->
    updateWeapon(player, item)
}

fun updateWeapon(player: Player, weapon: Item) {
    player["attack_range"] = 1
    player.weapon = weapon
}

on<CombatSwing>({ !swung() && isSword(it.weapon) }, Priority.LOWER) { player: Player ->
    player.setAnimation("sword_${
        when (player.attackType) {
            "slash", "block" -> "slash"
            else -> player.attackType
        }
    }")
    player.hit(target)
    delay = 4
}

on<CombatHit>({ isSword(weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("block")
}