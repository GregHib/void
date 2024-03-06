package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack
import kotlin.math.max

specialAttack("healing_blade") { player ->
    player.setAnimation(id)
    player.setGraphic(id)
    val damage = player.hit(target)
    if (damage != -1) {
        player.levels.restore(Skill.Constitution, max(100, damage / 20))
        player.levels.restore(Skill.Prayer, max(50, damage / 40))
    }
}