package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.specialAttackSwing
import world.gregs.voidps.world.interact.entity.player.combat.special.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy

specialAttackSwing("dragon_hatchet")  { player ->
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK)) {
        delay = -1
        return@specialAttackSwing
    }
    player.setAnimation("clobber")
    player.setGraphic("clobber")
    val damage = player.hit(target)
    if (damage != -1) {
        val drain = damage / 100
        if (drain > 0) {
            target.levels.drain(Skill.Defence, drain)
            target.levels.drain(Skill.Magic, drain)
        }
    }
    delay = 5
}