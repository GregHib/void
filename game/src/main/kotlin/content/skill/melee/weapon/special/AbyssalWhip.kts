package content.skill.melee.weapon.special

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import content.entity.player.combat.special.specialAttackHit
import content.entity.player.effect.energy.runEnergy

specialAttackHit("energy_drain") { player ->
    if (target !is Player) {
        return@specialAttackHit
    }
    val tenPercent = (target.runEnergy / 100) * 10
    if (tenPercent > 0) {
        target.runEnergy -= tenPercent
        player.runEnergy += tenPercent
        target.message("You feel drained!")
    }
}