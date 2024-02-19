package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.hit.block
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.specialAttackSwing
import world.gregs.voidps.world.interact.entity.combat.weaponSwing
import world.gregs.voidps.world.interact.entity.player.combat.special.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy

weaponSwing("dragon_dagger*", "corrupt_dragon_dagger*", priority = Priority.LOW) { player ->
    player.setAnimation("dragon_dagger_${
        when (player.attackType) {
            "slash" -> "slash"
            else -> "attack"
        }
    }")
    player.hit(target)
    delay = 4
}

block("dragon_dagger*", "corrupt_dragon_dagger*") {
    target.setAnimation("dragon_dagger_block", delay)
    blocked = true
}

specialAttackSwing("dragon_dagger*", "corrupt_dragon_dagger*") { player ->
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK / 4)) {
        delay = -1
        return@specialAttackSwing
    }
    player.setAnimation("puncture")
    player.setGraphic("puncture")
    player.hit(target)
    player.hit(target)
    delay = 4
}