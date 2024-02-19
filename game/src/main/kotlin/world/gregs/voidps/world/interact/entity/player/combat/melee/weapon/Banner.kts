package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.entity.combat.hit.block
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weaponSwing

weaponSwing("banner*", "rat_pole*", "*flag", priority = Priority.LOWER) { player ->
    player.setAnimation("banner_attack")
    player.hit(target)
    delay = 4
}

block("banner*", "rat_pole*", "*flag", priority = Priority.LOW) { _: Character ->
    target.setAnimation("banner_hit", delay)
    blocked = true
}