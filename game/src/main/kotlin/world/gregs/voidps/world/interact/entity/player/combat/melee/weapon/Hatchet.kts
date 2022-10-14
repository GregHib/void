package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*

fun isHatchet(item: Item?) = item != null && (item.id.endsWith("hatchet") || item.id.endsWith("battleaxe"))

on<CombatSwing>({ !swung() && isHatchet(it.weapon) }, Priority.LOWER) { player: Player ->
    player.setAnimation("hatchet_${
        when (player.attackType) {
            "block" -> "chop"
            else -> player.attackType
        }
    }")
    player.hit(target)
    delay = if (player.weapon.id.endsWith("battleaxe")) 6 else 5
}

on<CombatAttack>({ !blocked && target is Player && isHatchet(target.weapon) }, Priority.LOW) { _: Character ->
    target.setAnimation("hatchet_block", delay)
    blocked = true
}