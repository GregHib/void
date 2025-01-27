package content.skill.melee.weapon.special

import world.gregs.voidps.engine.entity.character.player.skill.Skill
import content.entity.player.combat.special.specialAttackDamage

specialAttackDamage("favour_of_the_war_god") { player ->
    val drain = damage / 10
    if (drain > 0) {
        target.levels.drain(Skill.Prayer, drain)
        player.levels.restore(Skill.Prayer, drain)
    }
}