package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.entity.combat.hit.block
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weaponSwing

weaponSwing("boxing_gloves*", Priority.LOW) { player ->
    player.setAnimation("boxing_gloves_attack")
    player.hit(target)
    delay = 4
}

block("boxing_gloves*", Priority.LOW) { _: Character ->
    target.setAnimation("boxing_gloves_block", delay)
    blocked = true
}