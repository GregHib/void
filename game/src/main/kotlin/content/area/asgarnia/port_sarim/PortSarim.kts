package content.area.asgarnia.port_sarim

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.item.floor.floorItemOperate
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.world.interact.entity.sound.playSound

val items: FloorItems by inject()

floorItemOperate("Take", "white_apron_port_sarim", override = false) {
    if (player.holdsItem("white_apron")) {
        player.message("You already have one of those.")
        cancel()
    } else if (player.inventory.isFull() && (!player.inventory.stackable(target.id) || !player.inventory.contains(target.id))) {
        player.inventoryFull()
        cancel()
    } else {
        player.anim("take")
        player.playSound("pickup_item")
        items.remove(target)
        player.inventory.add("white_apron")
        player.message("You take an apron. It feels freshly starched and smells of laundry.")
        cancel()
    }
}