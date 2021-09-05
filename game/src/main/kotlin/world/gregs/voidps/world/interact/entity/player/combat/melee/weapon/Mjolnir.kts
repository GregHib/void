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

fun isMjolnir(item: Item?) = item != null && item.name.endsWith("mjolnir")

on<CombatSwing>({ !swung() && isMjolnir(it.weapon) }, Priority.LOWER) { player: Player ->
    player.setAnimation("mjolnir_attack")
    player.hit(target)
    delay = 6
}

on<CombatHit>({ isMjolnir(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("mjolnir_hit")
}