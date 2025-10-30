package content.skill.melee.weapon.special

import content.entity.player.combat.special.specialAttackDamage
import content.skill.melee.weapon.drainByDamage
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class BoneDagger : Script {

    init {
        specialAttackDamage("backstab") {
            drainByDamage(target, damage, Skill.Defence)
        }
    }
}
