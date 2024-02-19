package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.specialAttackSwing
import world.gregs.voidps.world.interact.entity.player.combat.special.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy
import kotlin.math.max

specialAttackSwing("saradomin_godsword*") { player ->
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK / 2)) {
        delay = -1
        return@specialAttackSwing
    }
    player.setAnimation("healing_blade")
    player.setGraphic("healing_blade")
    val damage = player.hit(target)
    if (damage != -1) {
        player.levels.restore(Skill.Constitution, max(100, damage / 20))
        player.levels.restore(Skill.Prayer, max(50, damage / 40))
    }
    delay = 6
}