package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*

fun isZamorakianSpear(item: Item?) = item != null && item.id.startsWith("zamorakian_spear")

on<CombatSwing>({ !swung() && isZamorakianSpear(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("zamorakian_spear_${
        when (player.attackType) {
            "block" -> "lunge"
            else -> player.attackType
        }
    }")
    player.hit(target)
    delay = 4
}

on<CombatHit>({ !blocked && isZamorakianSpear(it.weapon) }) { player: Player ->
    player.setAnimation("zamorakian_spear_block")
    blocked = true
}