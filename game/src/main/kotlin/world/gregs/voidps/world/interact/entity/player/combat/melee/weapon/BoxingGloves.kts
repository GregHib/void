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

fun isBoxingGloves(item: Item?) = item != null && item.id.startsWith("boxing_gloves")

on<CombatSwing>({ !swung() && isBoxingGloves(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("boxing_gloves_attack")
    player.hit(target)
    delay = 4
}

on<CombatHit>({ !blocked && isBoxingGloves(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("boxing_gloves_block")
    blocked = true
}