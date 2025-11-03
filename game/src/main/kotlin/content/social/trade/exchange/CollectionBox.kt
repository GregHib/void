package content.social.trade.exchange

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.sendInventory

class CollectionBox : Script {

    val exchange: GrandExchange by inject()

    init {
        interfaceOpen("collection_box") { id ->
            for (slot in 0 until 6) {
                exchange.refresh(this, slot)
                interfaceOptions.unlockAll(id, "collection_box_$slot", 0..4)
                sendInventory("collection_box_$slot")
            }
        }
    }
}
