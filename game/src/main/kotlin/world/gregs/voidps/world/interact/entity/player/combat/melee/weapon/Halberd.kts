package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*

fun isHalberd(item: Item?) = item != null && item.name.endsWith("halberd")

on<CombatSwing>({ !swung() && isHalberd(it.weapon) }, Priority.LOWER) { player: Player ->
    player.setAnimation("halberd_${player.attackType}")
    player.hit(target)
    delay = 7
}

on<CombatHit>({ !blocked && isHalberd(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("spear_block")
    blocked = true
}