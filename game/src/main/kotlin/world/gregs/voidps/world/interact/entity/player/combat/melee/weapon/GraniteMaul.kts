package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.entity.combat.hit.block
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weaponSwing

weaponSwing("granite_maul*", Priority.LOW) { player ->
    player.setAnimation("granite_maul_attack")
    player.hit(target)
    delay = 7
}

block("granite_maul*") {
    target.setAnimation("granite_maul_block", delay)
    blocked = true
}