package content.social.trade.exchange

import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.inv.sendInventory

interfaceOpen("collection_box") { player ->
    player.interfaceOptions.unlockAll(id, "collect_slot_0")
    for (slot in 0 until 6) {
        player.sendInventory("collection_box_${slot}")
    }
}