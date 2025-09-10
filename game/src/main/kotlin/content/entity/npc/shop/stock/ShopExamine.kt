package content.entity.npc.shop.stock

import content.entity.npc.shop.shopInventory
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.event.Script
@Script
class ShopExamine {

    init {
        interfaceOption("Examine", "inventory", "shop_side") {
            val examine: String = item.def.getOrNull("examine") ?: return@interfaceOption
            player.message(examine)
        }

        interfaceOption("Examine", "sample", "shop") {
            val item = player.shopInventory(true)[itemSlot / 4]
            val examine: String = item.def.getOrNull("examine") ?: return@interfaceOption
            player.message(examine)
        }

        interfaceOption("Examine", "stock", "shop") {
            val item = player.shopInventory(false)[itemSlot / 6]
            val examine: String = item.def.getOrNull("examine") ?: return@interfaceOption
            player.message(examine)
        }

    }

}
