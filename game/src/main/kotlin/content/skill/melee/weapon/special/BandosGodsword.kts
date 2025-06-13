package content.skill.melee.weapon.special

import content.entity.player.combat.special.specialAttackDamage
import content.skill.melee.weapon.drainByDamage
import world.gregs.voidps.engine.entity.character.player.skill.Skill

specialAttackDamage("warstrike") {
    drainByDamage(target, damage, Skill.Defence, Skill.Strength, Skill.Prayer, Skill.Attack, Skill.Magic, Skill.Ranged)
}
