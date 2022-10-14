package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*

fun isScimitar(item: Item?) = item != null && (item.id.endsWith("scimitar") || item.id.endsWith("cutlass") || item.id == "brine_sabre" || item.id.endsWith("machete") || item.id.startsWith("silver_sickle"))

on<CombatSwing>({ !swung() && isScimitar(it.weapon) }, Priority.LOWER) { player: Player ->
    player.setAnimation("scimitar_${
        when (player.attackType) {
            "lunge" -> "lunge"
            else -> "slash"
        }
    }")
    player.hit(target)
    delay = 4
}

on<CombatAttack>({ !blocked && target is Player && isScimitar(target.weapon) }, Priority.LOW) { _: Character ->
    target.setAnimation("scimitar_block", delay)
    blocked = true
}