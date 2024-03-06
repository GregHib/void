package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.hit

combatSwing("vestas_longsword*", "melee", special = true) { player ->
    player.setAnimation("vestas_longsword_feint")
    player.hit(target)
}
