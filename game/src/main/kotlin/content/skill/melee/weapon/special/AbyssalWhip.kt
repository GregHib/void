package content.skill.melee.weapon.special

import content.entity.player.combat.special.SpecialAttack
import content.entity.player.effect.energy.runEnergy
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player

class AbyssalWhip : Script, SpecialAttack {
    init {
        specialAttackDamage("energy_drain") { target, damage ->
            if (target !is Player || damage < 0) {
                return@specialAttackDamage
            }
            val tenPercent = (target.runEnergy / 100) * 10
            if (tenPercent > 0) {
                target.runEnergy -= tenPercent
                runEnergy += tenPercent
                target.message("You feel drained!")
            }
        }
    }
}
