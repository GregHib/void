package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*

fun isPickaxe(item: Item?) = item != null && item.id.endsWith("pickaxe")

on<CombatSwing>({ !swung() && isPickaxe(it.weapon) }, Priority.LOWER) { player: Player ->
    player.setAnimation("pickaxe_${
        when (player.attackType) {
            "block" -> "spike"
            else -> player.attackType
        }
    }")
    player.hit(target)
    delay = 5
}

on<CombatAttack>({ !blocked && target is Player && isPickaxe(target.weapon) }, Priority.LOW) { _: Character ->
    target.setAnimation("pickaxe_block", delay)
    blocked = true
}