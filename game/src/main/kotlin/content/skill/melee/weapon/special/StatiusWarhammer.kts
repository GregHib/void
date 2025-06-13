package content.skill.melee.weapon.special

import content.entity.player.combat.special.specialAttackDamage
import world.gregs.voidps.engine.entity.character.player.skill.Skill

specialAttackDamage("smash") {
    target.levels.drain(Skill.Defence, multiplier = 0.30)
}
