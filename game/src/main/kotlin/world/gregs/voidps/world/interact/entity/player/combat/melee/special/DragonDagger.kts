package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.hit

combatSwing("dragon_dagger*", "melee", special = true) { player ->
    player.setAnimation("puncture")
    player.setGraphic("puncture")
    player.hit(target)
    player.hit(target)
}