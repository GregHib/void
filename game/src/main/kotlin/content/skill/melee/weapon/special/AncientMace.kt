package content.skill.melee.weapon.special

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class AncientMace : Script {
    init {
        specialAttackDamage("favour_of_the_war_god") { target, damage ->
            if (damage < 0) {
                return@specialAttackDamage
            }
            val drain = damage / 10
            if (drain > 0) {
                target.levels.drain(Skill.Prayer, drain)
                levels.restore(Skill.Prayer, drain)
            }
        }
    }
}
