package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*

fun isGodsword(item: Item?) = item != null && (item.name.endsWith("godsword") || item.name == "saradomin_sword")

on<CombatSwing>({ !swung() && isGodsword(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("godsword_${player.attackType}")
    player.hit(target)
    delay = 6
}

on<CombatHit>({ !blocked && isGodsword(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("godsword_hit")
    blocked = true
}