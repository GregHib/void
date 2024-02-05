package world.gregs.voidps.world.interact.entity.player.combat.melee.defence

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.hit.combatAttack

fun isShield(item: Item) = item.id.endsWith("shield")

combatAttack({ !blocked && target is Player && isShield(target.equipped(EquipSlot.Shield)) }, Priority.HIGH) { _: Character ->
    target.setAnimation("shield_block", delay)
    blocked = true
}