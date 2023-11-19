package world.gregs.voidps.world.interact.entity.player.combat.melee.defence

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.hit.CombatAttack

fun isBook(item: Item?) = item != null && item.id.endsWith("book")

on<CombatAttack>({ !blocked && target is Player && isBook(target.equipped(EquipSlot.Shield)) }, Priority.HIGH) { _: Character ->
    target.setAnimation("book_block", delay)
    blocked = true
}