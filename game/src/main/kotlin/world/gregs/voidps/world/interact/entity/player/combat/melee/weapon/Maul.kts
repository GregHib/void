package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatHit
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.combat.weapon

fun isMaul(item: Item?) = item != null && (item.id.endsWith("maul") || isTzhaarKetOm(item))
fun isTzhaarKetOm(item: Item) = item.id == "tzhaar_ket_om"

on<CombatSwing>({ !swung() && isMaul(it.weapon) }, Priority.LOWER) { player: Player ->
    player.setAnimation("maul_attack")
    player.hit(target)
    delay = if (isTzhaarKetOm(player.weapon)) 7 else 6
}

on<CombatHit>({ !blocked && isMaul(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("maul_block")
    blocked = true
}