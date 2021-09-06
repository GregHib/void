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

fun isBanner(item: Item?) = item != null && (item.name.startsWith("banner") || item.name.startsWith("rat_pole") || item.name.endsWith("flag"))

on<CombatSwing>({ !swung() && isBanner(it.weapon) }, Priority.LOWER) { player: Player ->
    player.setAnimation("banner_attack")
    player.hit(target)
    delay = 4
}

on<CombatHit>({ isBanner(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("banner_hit")
}