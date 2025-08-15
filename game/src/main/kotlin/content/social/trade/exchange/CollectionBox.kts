package content.social.trade.exchange

import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.inv.sendInventory

/*
    TODO
        Collection box collecting
        F2P
        Cs2 cache edits for limits changes?
        Messages
        Npcs
        Sets
        History
 */

interfaceOpen("collection_box") { player ->
    player.interfaceOptions.unlockAll(id, "box0")
    player.interfaceOptions.unlockAll(id, "collect_slot_0")
    for (slot in 0 until 6) {
        player.sendInventory("collection_box_$slot")
    }
}
