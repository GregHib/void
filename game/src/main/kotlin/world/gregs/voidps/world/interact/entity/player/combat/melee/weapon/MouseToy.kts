package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.combatAttack
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon

fun isToy(item: Item) = item.id.startsWith("mouse_toy")

combatSwing({ !swung() && isToy(it.weapon) }, Priority.LOWER) { player: Player ->
    player.setAnimation("mouse_toy_attack")
    player.hit(target)
    delay = 4
}

combatAttack({ !blocked && target is Player && isToy(target.weapon) }) { _: Character ->
    target.setAnimation("whip_block", delay)
    blocked = true
}