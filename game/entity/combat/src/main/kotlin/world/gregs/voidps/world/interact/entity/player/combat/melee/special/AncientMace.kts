package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttackHit

specialAttackHit("favour_of_the_war_god") { player ->
    val drain = damage / 10
    if (drain > 0) {
        target.levels.drain(Skill.Prayer, drain)
        player.levels.restore(Skill.Prayer, drain)
    }
}