package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.combat.weapon

fun isToragsHammers(item: Item?) = item != null && item.id.startsWith("torags_hammers")

on<CombatSwing>({ !swung() && isToragsHammers(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("torags_hammers_attack")
    player.hit(target)
    delay = 5
}