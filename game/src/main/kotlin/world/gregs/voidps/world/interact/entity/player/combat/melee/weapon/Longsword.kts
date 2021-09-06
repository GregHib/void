package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*

fun isLongsword(item: Item?) = item != null && (item.name.endsWith("longsword") || isExcalibur(item) || isFunWeapon(item) || item.name == "fremennik_blade" || isObsidianWeapon(item))
fun isExcalibur(item: Item) = item.name.startsWith("darklight") || item.name.startsWith("excalibur") || item.name == "enhanced_excalibur"
fun isFunWeapon(item: Item) = item.name == "wooden_spoon" || item.name == "skewer" || item.name == "cleaver"
fun isObsidianWeapon(item: Item) = item.name == "toktz-xil-ek"

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

on<CombatHit>({ isLongsword(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("longsword_block")
}