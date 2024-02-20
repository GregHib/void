package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.entity.combat.hit.block
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weaponSwing

weaponSwing("mouse_toy*", Priority.LOWER) { player ->
    player.setAnimation("mouse_toy_attack")
    player.hit(target)
    delay = 4
}

block("mouse_toy*") {
    target.setAnimation("whip_block", delay)
    blocked = true
}