package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.player.combat.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.melee.specialAccuracyMultiplier
import world.gregs.voidps.world.interact.entity.player.combat.melee.specialDamageMultiplier
import world.gregs.voidps.world.interact.entity.player.combat.specialAttack

fun isDragonDagger(item: Item?) = item != null && (item.name.startsWith("dragon_dagger") || item.name.startsWith("corrupt_dragon_dagger"))

on<CombatSwing>({ !swung() && !it.specialAttack && isDragonDagger(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("dragon_dagger_${
        when (player.attackType) {
            "slash" -> "slash"
            else -> "attack"
        }
    }")
    player.hit(target)
    delay = 4
}

on<CombatHit>({ isDragonDagger(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("dragon_dagger_block")
}

// Special attack

specialAccuracyMultiplier(1.15, ::isDragonDagger)

specialDamageMultiplier(1.15, ::isDragonDagger)

on<CombatSwing>({ !swung() && it.specialAttack && isDragonDagger(it.weapon) }) { player: Player ->
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK / 4)) {
        delay = -1
        return@on
    }
    player.setAnimation("puncture")
    player.setGraphic("puncture", height = 100)
    player.hit(target)
    player.hit(target)
    delay = 4
}