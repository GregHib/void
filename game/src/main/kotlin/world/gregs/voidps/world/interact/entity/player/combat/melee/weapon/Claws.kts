package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*

fun isClaws(item: Item?) = item != null && item.name.endsWith("claws")

on<CombatSwing>({ !swung() && isClaws(it.weapon) }, Priority.LOWER) { player: Player ->
    player.setAnimation("claw_${
        when (player.attackType) {
            "block" -> "slash"
            else -> player.attackType
        }
    }")
    player.hit(target)
    delay = 4
}

on<CombatHit>({ isClaws(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("claw_block")
}