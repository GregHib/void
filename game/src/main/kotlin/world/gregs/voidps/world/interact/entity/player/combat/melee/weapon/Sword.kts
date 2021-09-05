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

fun isWeapon(item: Item?) = item != null && (isSword(item) || item.name.endsWith("rapier") || isFunWeapon(item))
fun isSword(item: Item) = item.name.endsWith("sword") && item.name != "shadow_sword"
fun isFunWeapon(item: Item) = item.name == "spork" || item.name == "kitchen_knife"

on<Registered>({ isWeapon(it.equipped(EquipSlot.Weapon)) }) { player: Player ->
    updateWeapon(player, player.equipped(EquipSlot.Weapon))
}

on<ItemChanged>({ container == "worn_equipment" && index == EquipSlot.Weapon.index && isWeapon(item) }) { player: Player ->
    updateWeapon(player, item)
}

fun updateWeapon(player: Player, weapon: Item) {
    player["attack_range"] = 1
    player.weapon = weapon
}

on<CombatSwing>({ !swung() && isWeapon(it.weapon) }, Priority.LOWER) { player: Player ->
    player.setAnimation("sword_${
        when (player.attackType) {
            "slash", "block" -> "slash"
            else -> player.attackType
        }
    }")
    player.hit(target)
    delay = 4
}

on<CombatHit>({ isWeapon(weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("block")
}