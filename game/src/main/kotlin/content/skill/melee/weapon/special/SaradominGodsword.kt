package content.skill.melee.weapon.special

import content.entity.player.combat.special.specialAttackDamage
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Script
import kotlin.math.max

@Script
class SaradominGodsword {

    init {
        specialAttackDamage("healing_blade") { player ->
            player.levels.restore(Skill.Constitution, max(100, damage / 20))
            player.levels.restore(Skill.Prayer, max(50, damage / 40))
        }
    }
}
