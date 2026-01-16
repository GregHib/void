package content.skill.melee.weapon.special

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class StatiusWarhammer : Script {

    init {
        specialAttackDamage("smash") { target, damage ->
            if (damage >= 0) {
                target.levels.drain(Skill.Defence, multiplier = 0.30)
            }
        }
    }
}
