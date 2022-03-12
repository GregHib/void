package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*

fun isScythe(item: Item?) = item != null && item.id == "scythe"

on<CombatSwing>({ !swung() && isScythe(it.weapon) }, Priority.LOWER) { player: Player ->
    player.setAnimation("scythe_${player.attackType}")
    player.hit(target)
    delay = 6
}

on<CombatHit>({ !blocked && isScythe(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("scythe_hit")
    blocked = true
}