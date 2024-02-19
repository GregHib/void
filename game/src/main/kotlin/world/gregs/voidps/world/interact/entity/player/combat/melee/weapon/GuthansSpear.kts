package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.hit.block
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weaponSwing

weaponSwing("guthans_warspear*", Priority.LOW) { player ->
    player.setAnimation("guthans_spear_${
        when (player.attackType) {
            "swipe" -> "swipe"
            else -> "attack"
        }
    }")
    player.hit(target)
    delay = 5
}

block("guthans_warspear*") {
    target.setAnimation("guthans_spear_block", delay)
    blocked = true
}