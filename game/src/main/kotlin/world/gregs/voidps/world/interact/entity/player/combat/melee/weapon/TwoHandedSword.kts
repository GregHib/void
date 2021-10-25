package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*

fun is2hSword(item: Item?) = item != null && (item.id.endsWith("2h_sword") || item.id == "shadow_sword" || isFunWeapon(item))
fun isFunWeapon(item: Item) = item.id == "giants_hand" || item.id == "spatula"

on<CombatSwing>({ !swung() && is2hSword(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("2h_sword_${player.attackType}")
    player.hit(target)
    delay = 7
}

on<CombatHit>({ !blocked && is2hSword(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("2h_sword_hit")
    blocked = true
}