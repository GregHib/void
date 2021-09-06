package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*

fun isWarhammer(item: Item?) = item != null && (item.name.endsWith("warhammer") || isFunWeapon(item) || isBlackJack(item))
fun isBlackJack(item: Item) = item.name.endsWith("blackjack") || item.name.endsWith("blackjack_o") || item.name.endsWith("blackjack_d")
fun isFunWeapon(item: Item) = item.name.startsWith("rubber_chicken") || item.name.startsWith("magnifying_glass") || item.name == "bone_club"

on<CombatSwing>({ !swung() && isWarhammer(it.weapon) }, Priority.LOWER) { player: Player ->
    player.setAnimation("warhammer_${
        when (player.attackType) {
            "pummel" -> "pummel" 
            else -> "pound"
        }
    }")
    player.hit(target)
    delay = 6
}

on<CombatHit>({ isWarhammer(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("warhammer_block")
}