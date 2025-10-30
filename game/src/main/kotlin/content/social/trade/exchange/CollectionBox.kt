package content.social.trade.exchange

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.sendInventory

class CollectionBox : Script {

    val exchange: GrandExchange by inject()

    init {
        interfaceOpen("collection_box") { player ->
            for (slot in 0 until 6) {
                exchange.refresh(player, slot)
                player.interfaceOptions.unlockAll(id, "collection_box_$slot", 0..4)
                player.sendInventory("collection_box_$slot")
            }
        }
    }
}
