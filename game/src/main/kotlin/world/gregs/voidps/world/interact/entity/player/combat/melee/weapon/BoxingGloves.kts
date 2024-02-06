package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.entity.combat.hit.combatAttack
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.combat.weaponSwing

fun isBoxingGloves(item: Item) = item.id.startsWith("boxing_gloves")

weaponSwing("boxing_gloves*", Priority.LOW) { player: Player ->
    player.setAnimation("boxing_gloves_attack")
    player.hit(target)
    delay = 4
}

combatAttack({ !blocked && target is Player && isBoxingGloves(target.weapon) }, Priority.LOW) { _: Character ->
    target.setAnimation("boxing_gloves_block", delay)
    blocked = true
}