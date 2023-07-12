package world.gregs.voidps.world.interact.entity.item

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.ItemOnObject
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.clear
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.suspend.arriveDelay
import world.gregs.voidps.world.interact.entity.sound.playSound

val floorItems: FloorItems by inject()

on<ItemOnObject>({ operate && obj.id.startsWith("table") }) { player: Player ->
    arriveDelay()
    if (!World.members && item.def["members", false]) {
        player.message("To use this item please login to a members' server.")
        return@on
    }
    if (!item.tradeable) {
        player.message("You cannot put that on a table.")
        return@on
    }
    if (player.inventory.clear(itemSlot)) {
        player.setAnimation("take")
        player.playSound("drop_item")
        val tile = obj.nearestTo(player.tile)
        floorItems.add(tile, item.id, item.amount, revealTicks = 100, disappearTicks = 1000, owner = player)
    }
}