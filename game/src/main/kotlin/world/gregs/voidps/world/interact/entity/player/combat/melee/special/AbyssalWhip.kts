package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.hit.block
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weaponSwing
import world.gregs.voidps.world.interact.entity.player.combat.special.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack
import world.gregs.voidps.world.interact.entity.player.energy.runEnergy

weaponSwing("abyssal_whip*", Priority.LOW) { player ->
    if (player.specialAttack && !drainSpecialEnergy(player, MAX_SPECIAL_ATTACK / 2)) {
        delay = -1
        return@weaponSwing
    }
    player.setAnimation("whip_${if (player.specialAttack) "special" else player.attackType}")
    if (player.hit(target) != -1 && player.specialAttack) {
        if (target is Player) {
            val tenPercent = (target.runEnergy / 100) * 10
            if (tenPercent > 0) {
                target.runEnergy -= tenPercent
                player.runEnergy += tenPercent
                target.message("You feel drained!")
            }
        }
        target.setGraphic("energy_drain")
    }
    delay = 4
}

block("abyssal_whip*") {
    target.setAnimation("whip_block", delay)
    blocked = true
}