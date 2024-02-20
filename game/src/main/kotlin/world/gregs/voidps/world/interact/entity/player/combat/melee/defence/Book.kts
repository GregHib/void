package world.gregs.voidps.world.interact.entity.player.combat.melee.defence

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.hit.block

block(Priority.HIGH) { _ ->
    if (target is Player && target.equipped(EquipSlot.Shield).id.endsWith("book")) {
        target.setAnimation("book_block", delay)
        blocked = true
    }
}