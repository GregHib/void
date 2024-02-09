package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.entity.combat.hit.block
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weaponSwing

weaponSwing("veracs_flail*", Priority.LOW) { player: Player ->
    player.setAnimation("veracs_flail_attack")
    player.hit(target)
    delay = 5
}

block("veracs_flail*") {
    target.setAnimation("veracs_flail_block", delay)
    blocked = true
}