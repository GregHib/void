package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*

fun isWeapon(item: Item?) = item != null && (isSpear(item) || isHastae(item))

fun isHastae(item: Item) = item.id.endsWith("hasta") || item.id.endsWith("hasta_p") || item.id.endsWith("hasta_p+") || item.id.endsWith("hasta_p++")

fun isSpear(item: Item) = item.id.endsWith("spear") || item.id.endsWith("spear_p") || item.id.endsWith("spear_p+") || item.id.endsWith("spear_p++")

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

on<CombatAttack>({ !blocked && target is Player && isWeapon(target.weapon) }, Priority.LOW) { _: Character ->
    target.setAnimation("spear_block", delay)
    blocked = true
}