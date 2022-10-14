package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*

fun isHalberd(item: Item?) = item != null && item.id.endsWith("halberd")

on<CombatSwing>({ !swung() && isHalberd(it.weapon) }, Priority.LOWER) { player: Player ->
    player.setAnimation("halberd_${player.attackType}")
    player.hit(target)
    delay = 7
}

on<CombatAttack>({ !blocked && target is Player && isHalberd(target.weapon) }, Priority.LOW) { _: Character ->
    target.setAnimation("spear_block", delay)
    blocked = true
}