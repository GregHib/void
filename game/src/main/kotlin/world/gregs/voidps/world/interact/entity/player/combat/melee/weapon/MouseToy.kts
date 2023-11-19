package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.hit.CombatAttack
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon

fun isToy(item: Item?) = item != null && item.id.startsWith("mouse_toy")

on<CombatSwing>({ !swung() && isToy(it.weapon) }, Priority.LOWER) { player: Player ->
    player.setAnimation("mouse_toy_attack")
    player.hit(target)
    delay = 4
}

on<CombatAttack>({ !blocked && target is Player && isToy(target.weapon) }) { _: Character ->
    target.setAnimation("whip_block", delay)
    blocked = true
}