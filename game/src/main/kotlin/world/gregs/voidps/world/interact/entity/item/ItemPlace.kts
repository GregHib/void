package world.gregs.voidps.world.interact.entity.item

import world.gregs.voidps.engine.client.ui.interact.InterfaceOnObject
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.item.FloorItems
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.Distance.getNearest
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.world.interact.entity.sound.playSound

val items: FloorItems by inject()

on<InterfaceOnObject>({ obj.id.startsWith("drawers") || obj.id.startsWith("table") || obj.id.startsWith("counter") }) { player: Player ->
    if (player.inventory.remove(itemSlot, item.id, item.amount)) {
        player.setAnimation("take")
        player.playSound("drop_item")
        items.add(item.id, item.amount, getNearest(obj.tile, obj.size, player.tile), 100, 1000, player)
    }
}