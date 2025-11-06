package content.skill.melee.weapon.special

import content.entity.player.combat.special.SpecialAttack
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class StatiusWarhammer : Script, SpecialAttack {
    init {
        specialAttackDamage("smash") { target, damage ->
            if (damage >= 0) {
                target.levels.drain(Skill.Defence, multiplier = 0.30)
            }
        }
    }
}
