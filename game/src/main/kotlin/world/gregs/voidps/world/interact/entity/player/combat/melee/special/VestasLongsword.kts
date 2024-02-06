package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.hit.block
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.specialAttackSwing
import world.gregs.voidps.world.interact.entity.combat.weaponSwing
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack

weaponSwing("*vestas_longsword", Priority.LOW) { player: Player ->
    player.setAnimation("vestas_longsword_${
        when (player.attackType) {
            "lunge" -> "lunge"
            else -> "attack"
        }
    }")
    player.hit(target)
    delay = 5
}

block("*vestas_longsword") {
    target.setAnimation("vestas_longsword_block", delay)
    blocked = true
}

// Special attack
specialAttackSwing("*vestas_longsword") { player: Player ->
    if (player.specialAttack && !drainSpecialEnergy(player, 250)) {
        delay = -1
        return@specialAttackSwing
    }
    player.setAnimation("vestas_longsword_feint")
    player.hit(target)
    delay = 5
}
