package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*

fun isLongsword(item: Item?) = item != null && (item.id.endsWith("longsword") || isExcalibur(item) || isFunWeapon(item) || item.id == "fremennik_blade" || isObsidianWeapon(item))
fun isExcalibur(item: Item) = item.id.startsWith("darklight") || item.id.startsWith("excalibur") || item.id == "enhanced_excalibur"
fun isFunWeapon(item: Item) = item.id == "wooden_spoon" || item.id == "skewer" || item.id == "cleaver"
fun isObsidianWeapon(item: Item) = item.id == "toktz_xil_ek"

on<CombatSwing>({ !swung() && isLongsword(it.weapon) }, Priority.LOWER) { player: Player ->
    player.setAnimation("longsword_${
        when (player.attackType) {
            "slash", "block" -> "slash"
            else -> player.attackType
        }
    }")
    player.hit(target)
    delay = 5
}

on<CombatHit>({ !blocked && isLongsword(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("longsword_block")
    blocked = true
}