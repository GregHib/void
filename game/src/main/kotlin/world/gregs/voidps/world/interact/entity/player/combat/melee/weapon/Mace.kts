package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*

fun isMace(item: Item?) = item != null && (item.id.endsWith("mace") || item.id.endsWith("cane") || isFunWeapon(item) || item.id == "tzhaar_ket_em")
fun isFunWeapon(item: Item) = item.id == "frying_pan" || item.id == "rolling_pin" || item.id == "meat_tenderiser" || item.id == "undead_chicken"

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

on<CombatAttack>({ !blocked && target is Player && isMace(target.weapon) }, Priority.LOW) { _: Character ->
    target.setAnimation("mace_block", delay)
    blocked = true
}