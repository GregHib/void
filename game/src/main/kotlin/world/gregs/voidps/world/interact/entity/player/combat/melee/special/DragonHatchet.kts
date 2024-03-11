package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttackHit

specialAttackHit("clobber") {
    val drain = damage / 100
    if (drain > 0) {
        target.levels.drain(Skill.Defence, drain)
        target.levels.drain(Skill.Magic, drain)
    }
}