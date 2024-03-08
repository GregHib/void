package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.world.interact.entity.player.combat.melee.drainByDamage
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttackHit

specialAttackHit("warstrike") {
    drainByDamage(target, damage, Skill.Defence, Skill.Strength, Skill.Prayer, Skill.Attack, Skill.Magic, Skill.Ranged)
}
