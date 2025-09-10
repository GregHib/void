package content.skill.melee.weapon.special

import content.entity.player.combat.special.specialAttackDamage
import content.entity.player.effect.energy.runEnergy
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Script
@Script
class AbyssalWhip {

    init {
        specialAttackDamage("energy_drain") { player ->
            if (target !is Player) {
                return@specialAttackDamage
            }
            val tenPercent = (target.runEnergy / 100) * 10
            if (tenPercent > 0) {
                target.runEnergy -= tenPercent
                player.runEnergy += tenPercent
                target.message("You feel drained!")
            }
        }

    }

}
