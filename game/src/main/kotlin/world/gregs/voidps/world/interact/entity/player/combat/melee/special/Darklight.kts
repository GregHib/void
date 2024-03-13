package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.world.interact.entity.combat.Target
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttackHit

specialAttackHit("weaken") {
    val amount = if (Target.isDemon(target)) 0.10 else 0.05
    target.levels.drain(Skill.Attack, multiplier = amount)
    target.levels.drain(Skill.Strength, multiplier = amount)
    target.levels.drain(Skill.Defence, multiplier = amount)
}