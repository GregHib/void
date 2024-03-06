package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.hit

combatSwing("dragon_mace*", "melee", special = true) { player ->
    player.setAnimation("shatter")
    player.setGraphic("shatter")
    player.hit(target)
    delay = 4
}