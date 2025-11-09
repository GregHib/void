package content.skill.melee.weapon.special

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import kotlin.math.max

class SaradominGodsword : Script {
    init {
        specialAttackDamage("healing_blade") { _, damage ->
            if (damage >= 0) {
                levels.restore(Skill.Constitution, max(100, damage / 20))
                levels.restore(Skill.Prayer, max(50, damage / 40))
            }
        }
    }
}
