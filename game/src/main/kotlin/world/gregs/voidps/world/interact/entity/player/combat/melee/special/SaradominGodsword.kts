package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import kotlin.math.max

combatSwing("saradomin_godsword*", "melee", special = true) { player ->
    player.setAnimation("healing_blade")
    player.setGraphic("healing_blade")
    val damage = player.hit(target)
    if (damage != -1) {
        player.levels.restore(Skill.Constitution, max(100, damage / 20))
        player.levels.restore(Skill.Prayer, max(50, damage / 40))
    }
    delay = 6
}