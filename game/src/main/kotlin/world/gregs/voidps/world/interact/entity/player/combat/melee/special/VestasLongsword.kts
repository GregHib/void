package world.gregs.voidps.world.interact.entity.player.combat.melee.special

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
import world.gregs.voidps.world.interact.entity.player.combat.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.melee.specialDamageMultiplier
import world.gregs.voidps.world.interact.entity.player.combat.specialAttack

fun isVestas(item: Item?) = item != null && item.name.endsWith("vestas_longsword")

on<Registered>({ isVestas(it.equipped(EquipSlot.Weapon)) }) { player: Player ->
    updateWeapon(player, player.equipped(EquipSlot.Weapon))
}

on<ItemChanged>({ container == "worn_equipment" && index == EquipSlot.Weapon.index && isVestas(item) }) { player: Player ->
    updateWeapon(player, item)
}

fun updateWeapon(player: Player, weapon: Item) {
    player["attack_range"] = 1
    player.weapon = weapon
}

on<CombatSwing>({ !swung() && isVestas(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("vestas_longsword_${
        when (player.attackType) {
            "lunge" -> "lunge"
            else -> "attack"
        }
    }")
    player.hit(target)
    delay = 5
}

on<CombatHit>({ isVestas(weapon) }) { player: Player ->
    player.setAnimation("vestas_longsword_block")
}

// Special attack

specialDamageMultiplier(1.2, ::isVestas)

on<CombatSwing>({ !swung() && it.specialAttack && isVestas(it.weapon) }) { player: Player ->
    if (player.specialAttack && !drainSpecialEnergy(player, 250)) {
        delay = -1
        return@on
    }
    player.setAnimation("vestas_longsword_feint")
    player.hit(target)
    delay = 5
}
