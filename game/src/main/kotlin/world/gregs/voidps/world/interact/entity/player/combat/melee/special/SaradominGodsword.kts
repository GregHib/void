package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.player.skill.Skill
import content.entity.player.combat.special.specialAttackHit
import kotlin.math.max

specialAttackHit("healing_blade") { player ->
    player.levels.restore(Skill.Constitution, max(100, damage / 20))
    player.levels.restore(Skill.Prayer, max(50, damage / 40))
}