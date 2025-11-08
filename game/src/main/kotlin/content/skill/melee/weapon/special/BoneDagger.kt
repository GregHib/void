package content.skill.melee.weapon.special

import content.skill.melee.weapon.drainByDamage
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class BoneDagger : Script {
    init {
        specialAttackDamage("backstab") { target, damage ->
            if (damage >= 0) {
                drainByDamage(target, damage, Skill.Defence)
            }
        }
    }
}
