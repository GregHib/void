package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.specialAttackSwing
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy

specialAttackSwing("*statiuss_warhammer", priority = Priority.LOW) { player: Player ->
    if (!drainSpecialEnergy(player, 350)) {
        delay = -1
        return@specialAttackSwing
    }
    player.setAnimation("statius_warhammer_smash")
    player.setGraphic("statius_warhammer_smash")
    if (player.hit(target) != -1) {
        target.levels.drain(Skill.Defence, multiplier = 0.30)
    }
    delay = 6
}