package world.gregs.voidps.world.map.port_sarim

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.contain.add
import world.gregs.voidps.engine.contain.hasItem
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.floor.FloorItemOption
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.entity.sound.playSound

val items: FloorItems by inject()
on<FloorItemOption>({  item.id == "white_apron_port_sarim" && option == "Take" }, Priority.HIGH){ player: Player ->
    if (player.hasItem("white_apron")) {
        player.message("You already have one of those.")
        cancel()
    } else if (player.inventory.isFull() && (!player.inventory.stackable(item.id) || !player.inventory.contains(item.id))) {
        player.inventoryFull()
        cancel()
    } else {
        player.setAnimation("take")
        player.playSound("pickup_item")
        items.remove(item)
        player.inventory.add("white_apron")
        player.message("You take an apron. It feels freshly starched and smells of laundry.")
        cancel()
    }
}