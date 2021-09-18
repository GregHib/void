package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*

fun isHatchet(item: Item?) = item != null && (item.name.endsWith("hatchet") || item.name.endsWith("battleaxe"))

on<CombatSwing>({ !swung() && isHatchet(it.weapon) }, Priority.LOWER) { player: Player ->
    player.setAnimation("hatchet_${
        when (player.attackType) {
            "block" -> "chop"
            else -> player.attackType
        }
    }")
    player.hit(target)
    delay = if (player.weapon.name.endsWith("battleaxe")) 6 else 5
}

on<CombatHit>({ !blocked && isHatchet(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("hatchet_block")
    blocked = true
}