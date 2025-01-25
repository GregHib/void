package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.player.skill.Skill
import content.entity.player.combat.special.specialAttackHit

specialAttackHit("smash") {
    target.levels.drain(Skill.Defence, multiplier = 0.30)
}