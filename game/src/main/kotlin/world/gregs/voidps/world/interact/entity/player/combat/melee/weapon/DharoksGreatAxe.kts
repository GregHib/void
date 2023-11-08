package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*

fun isGreatAxe(item: Item?) = item != null && (item.id.startsWith("dharoks_greataxe") || item.id == "balmung")

on<CombatSwing>({ !swung() && isGreatAxe(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("dharoks_greataxe_${
        when (player.attackType) {
            "smash" -> "smash"
            else -> "attack"
        }
    }")
    player.hit(target)
    delay = 7
}

on<CombatAttack>({ !blocked && target is Player && isGreatAxe(target.weapon) }, Priority.LOW) { _: Character ->
    target.setAnimation("dharoks_greataxe_block", delay)
    blocked = true
}