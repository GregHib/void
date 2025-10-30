package content.skill.melee.weapon.special

import content.entity.combat.Target
import content.entity.player.combat.special.specialAttackDamage
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class Darklight : Script {

    init {
        specialAttackDamage("weaken") {
            val amount = if (Target.isDemon(target)) 0.10 else 0.05
            target.levels.drain(Skill.Attack, multiplier = amount)
            target.levels.drain(Skill.Strength, multiplier = amount)
            target.levels.drain(Skill.Defence, multiplier = amount)
        }
    }
}
