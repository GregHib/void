package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatHit
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.combat.weapon

fun isMaul(item: Item?) = item != null && item.id.startsWith("granite_maul")

on<CombatSwing>({ !swung() && isMaul(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("granite_maul_attack")
    player.hit(target)
    delay = 7
}

on<CombatHit>({ !blocked && isMaul(it.weapon) }) { player: Player ->
    player.setAnimation("granite_maul_block")
    blocked = true
}