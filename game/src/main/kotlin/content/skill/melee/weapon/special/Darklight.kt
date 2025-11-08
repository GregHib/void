package content.skill.melee.weapon.special

import content.entity.combat.Target
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class Darklight : Script {
    init {
        specialAttackDamage("weaken") { target, damage ->
            if (damage < 0) {
                return@specialAttackDamage
            }
            val amount = if (Target.isDemon(target)) 0.10 else 0.05
            target.levels.drain(Skill.Attack, multiplier = amount)
            target.levels.drain(Skill.Strength, multiplier = amount)
            target.levels.drain(Skill.Defence, multiplier = amount)
        }
    }
}
