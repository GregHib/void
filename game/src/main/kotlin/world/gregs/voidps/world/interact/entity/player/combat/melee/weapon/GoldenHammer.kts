package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.entity.combat.hit.block
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weaponSwing

weaponSwing("golden_hammer", Priority.LOW) { player ->
    player.setAnimation("tzhaar_ket_om_attack")
    player.hit(target)
    delay = 6
}

block("golden_hammer") {
    target.setAnimation("tzhaar_ket_om_block", delay)
    blocked = true
}