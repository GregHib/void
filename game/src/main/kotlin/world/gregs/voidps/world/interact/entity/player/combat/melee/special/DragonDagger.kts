package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.hit.CombatAttack
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.special.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack

fun isDragonDagger(item: Item?) = item != null && (item.id.startsWith("dragon_dagger") || item.id.startsWith("corrupt_dragon_dagger"))

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

on<CombatAttack>({ !blocked && target is Player && isDragonDagger(target.weapon) }) { _: Character ->
    target.setAnimation("dragon_dagger_block", delay)
    blocked = true
}

on<CombatSwing>({ !swung() && it.specialAttack && isDragonDagger(it.weapon) }) { player: Player ->
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK / 4)) {
        delay = -1
        return@on
    }
    player.setAnimation("puncture")
    player.setGraphic("puncture")
    player.hit(target)
    player.hit(target)
    delay = 4
}