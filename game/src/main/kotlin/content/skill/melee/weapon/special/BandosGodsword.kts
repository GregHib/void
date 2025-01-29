package content.skill.melee.weapon.special

import world.gregs.voidps.engine.entity.character.player.skill.Skill
import content.skill.melee.weapon.drainByDamage
import content.entity.player.combat.special.specialAttackDamage

specialAttackDamage("warstrike") {
    drainByDamage(target, damage, Skill.Defence, Skill.Strength, Skill.Prayer, Skill.Attack, Skill.Magic, Skill.Ranged)
}
