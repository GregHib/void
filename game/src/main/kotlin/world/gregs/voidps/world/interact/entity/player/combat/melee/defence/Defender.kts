package world.gregs.voidps.world.interact.entity.player.combat.melee.defence

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.visual.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.CombatHit

fun isDefender(item: Item?) = item != null && item.id.endsWith("defender")

on<CombatHit>({ !blocked && isDefender(it.equipped(EquipSlot.Shield)) }, Priority.HIGH) { player: Player ->
    player.setAnimation("defender_block")
    blocked = true
}