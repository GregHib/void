package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.hit.block
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weaponSwing

weaponSwing("dharoks_greataxe*", "balmung", priority = Priority.LOW) { player ->
    player.setAnimation("dharoks_greataxe_${
        when (player.attackType) {
            "smash" -> "smash"
            else -> "attack"
        }
    }")
    player.hit(target)
    delay = 7
}

block("dharoks_greataxe*", "balmung", priority = Priority.LOW) {
    target.setAnimation("dharoks_greataxe_block", delay)
    blocked = true
}