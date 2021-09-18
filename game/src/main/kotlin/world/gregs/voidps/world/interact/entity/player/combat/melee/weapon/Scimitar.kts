package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*

fun isScimitar(item: Item?) = item != null && (item.name.endsWith("scimitar") || item.name.endsWith("cutlass") || item.name == "brine_sabre" || item.name.endsWith("machete") || item.name.startsWith("silver_sickle"))

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

on<CombatHit>({ !blocked && isScimitar(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("scimitar_block")
    blocked = true
}