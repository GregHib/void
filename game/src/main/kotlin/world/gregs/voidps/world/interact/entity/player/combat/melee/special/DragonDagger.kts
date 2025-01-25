package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.world.interact.entity.combat.hit.hit
import content.entity.player.combat.special.specialAttackHit

specialAttackHit("puncture", noHit = false) { player ->
    player.hit(target)
}