package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*

fun isWeapon(item: Item?) = item != null && (isSpear(item) || isHastae(item))

fun isHastae(item: Item) = item.name.endsWith("hasta") || item.name.endsWith("hasta_p") || item.name.endsWith("hasta_p+") || item.name.endsWith("hasta_p++")

fun isSpear(item: Item) = item.name.endsWith("spear") || item.name.endsWith("spear_p") || item.name.endsWith("spear_p+") || item.name.endsWith("spear_p++")

on<CombatSwing>({ !swung() && isWeapon(it.weapon) }, Priority.LOWER) { player: Player ->
    player.setAnimation("spear_${
        when (player.attackType) {
            "block" -> "lunge"
            else -> player.attackType
        }
    }")
    player.hit(target)
    delay = 4
}

on<CombatHit>({ !blocked && isWeapon(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("spear_block")
    blocked = true
}