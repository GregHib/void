package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.hit.block
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weaponSwing

weaponSwing("*godsword", "saradomin_sword", priority = Priority.LOW) { player ->
    player.setAnimation("godsword_${player.attackType}")
    player.hit(target)
    delay = 6
}

block("*godsword", "saradomin_sword", priority = Priority.LOW) {
    target.setAnimation("godsword_hit", delay)
    blocked = true
}