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

fun isIvandisFlail(item: Item?) = item != null && item.name.startsWith("ivandis_flail")

on<CombatSwing>({ !swung() && isIvandisFlail(it.weapon) }, Priority.LOWER) { player: Player ->
    player.setAnimation("flail_whack")
    player.hit(target)
    delay = 5
}

on<CombatHit>({ isIvandisFlail(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("flail_hit")
}