package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack

specialAttack("puncture") { player ->
    player.setAnimation(id)
    player.setGraphic(id)
    player.hit(target)
    player.hit(target)
}