package world.gregs.voidps.world.interact.entity.item

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnObject
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.FloorItems
import world.gregs.voidps.engine.entity.members
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.Distance.getNearest
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.world.interact.entity.sound.playSound

val items: FloorItems by inject()

on<InterfaceOnObject>({ obj.id.startsWith("table") }) { player: Player ->
    if (!World.members && item.def["members", false]) {
        player.message("To use this item please login to a members' server.")
        return@on
    }
    if (!item.tradeable) {
        player.message("You cannot put that on a table.")
        return@on
    }
    if (player.inventory.remove(itemSlot, item.id, item.amount)) {
        player.setAnimation("take")
        player.playSound("drop_item")
        items.add(item.id, item.amount, getNearest(obj.tile, obj.size, player.tile), 100, 1000, player)
    }
}