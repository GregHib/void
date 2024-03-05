package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weaponSwing

weaponSwing("zamorakian_spear*", Priority.LOW) { player ->
    player.setAnimation("zamorakian_spear_${
        when (player.attackType) {
            "block" -> "lunge"
            else -> player.attackType
        }
    }")
    player.hit(target)
    delay = 4
}