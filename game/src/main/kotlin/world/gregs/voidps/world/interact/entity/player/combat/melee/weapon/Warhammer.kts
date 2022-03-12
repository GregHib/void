package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*

fun isWarhammer(item: Item?) = item != null && (item.id.endsWith("warhammer") || isFunWeapon(item) || isBlackJack(item))
fun isBlackJack(item: Item) = item.id.endsWith("blackjack") || item.id.endsWith("blackjack_o") || item.id.endsWith("blackjack_d")
fun isFunWeapon(item: Item) = item.id.startsWith("rubber_chicken") || item.id.startsWith("magnifying_glass") || item.id == "bone_club"

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

on<CombatHit>({ !blocked && isWarhammer(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("warhammer_block")
    blocked = true
}