package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*

fun isWarSpear(item: Item?) = item != null && item.id.startsWith("guthans_warspear")

on<CombatSwing>({ !swung() && isWarSpear(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("guthans_spear_${
        when (player.attackType) {
            "swipe" -> "swipe"
            else -> "attack"
        }
    }")
    player.hit(target)
    delay = 5
}

on<CombatAttack>({ !blocked && target is Player && isWarSpear(target.weapon) }) { _: Character ->
    target.setAnimation("guthans_spear_block", delay)
    blocked = true
}