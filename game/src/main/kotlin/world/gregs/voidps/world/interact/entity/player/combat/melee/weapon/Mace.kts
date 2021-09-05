package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*

fun isMace(item: Item?) = item != null && (item.name.endsWith("mace") || item.name.endsWith("cane") || isFunWeapon(item) || item.name == "tzhaar-ket-em")
fun isFunWeapon(item: Item) = item.name == "frying_pan" || item.name == "rolling_pin" || item.name == "meat_tenderiser" || item.name == "undead_chicken"

on<CombatSwing>({ !swung() && isMace(it.weapon) }, Priority.LOWER) { player: Player ->
    player.setAnimation("mace_${
        when (player.attackType) {
            "pummel", "block", "bash", "focus" -> "pound"
            else -> player.attackType
        }
    }")
    player.hit(target)
    delay = 4
}

on<CombatHit>({ isMace(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("mace_block")
}