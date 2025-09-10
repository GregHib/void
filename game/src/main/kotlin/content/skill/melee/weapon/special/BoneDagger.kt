package content.skill.melee.weapon.special

import content.entity.player.combat.special.specialAttackDamage
import content.skill.melee.weapon.drainByDamage
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Script
@Script
class BoneDagger {

    init {
        specialAttackDamage("backstab") {
            drainByDamage(target, damage, Skill.Defence)
        }

    }

}
