package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.player.combat.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.melee.specialAccuracyMultiplier
import world.gregs.voidps.world.interact.entity.player.combat.melee.specialDamageMultiplier
import world.gregs.voidps.world.interact.entity.player.combat.specialAttack

fun isSaradominSword(weapon: Item?) = weapon != null && weapon.id.startsWith("saradomin_sword")

specialDamageMultiplier(1.1, ::isSaradominSword)
specialAccuracyMultiplier(2.0, ::isSaradominSword)

on<CombatSwing>({ !swung() && it.specialAttack && isSaradominSword(it.weapon) }) { player: Player ->
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK)) {
        delay = -1
        return@on
    }
    player.setAnimation("saradomins_lightning")
    val weapon = player.weapon
    val damage = hit(player, target, "melee", weapon)
    player.hit(target, damage = damage)
    if (damage > 0) {
        player.hit(target, type = "magic")
    }
    delay = 4
}

on<CombatHit>({ isSaradominSword(weapon) && special && type == "melee" }) { character: Character ->
    character.setGraphic("saradomins_lightning")
}

on<HitDamageModifier>({ type == "magic" && isSaradominSword(weapon) }, Priority.LOWER) { _: Player ->
    damage = 160.0
}