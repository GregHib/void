package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack

specialAttack("smash") { player ->
    player.setAnimation(id)
    player.setGraphic(id)
    if (player.hit(target) != -1) {
        target.levels.drain(Skill.Defence, multiplier = 0.30)
    }
}