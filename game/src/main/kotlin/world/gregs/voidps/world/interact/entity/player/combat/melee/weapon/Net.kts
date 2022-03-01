package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*

fun isNet(item: Item?) = item != null && (item.id.startsWith("butterfly_net") || item.id.endsWith("butterfly_net") || item.id == "noose_wand")

on<CombatSwing>({ !swung() && isNet(it.weapon) }, Priority.LOWER) { player: Player ->
    player.setAnimation("net_${player.attackType}")
    player.hit(target)
    delay = 4
}

on<CombatHit>({ !blocked && isNet(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("net_hit")
    blocked = true
}