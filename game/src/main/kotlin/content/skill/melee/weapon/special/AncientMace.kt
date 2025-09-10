package content.skill.melee.weapon.special

import content.entity.player.combat.special.specialAttackDamage
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Script
@Script
class AncientMace {

    init {
        specialAttackDamage("favour_of_the_war_god") { player ->
            val drain = damage / 10
            if (drain > 0) {
                target.levels.drain(Skill.Prayer, drain)
                player.levels.restore(Skill.Prayer, drain)
            }
        }

    }

}
