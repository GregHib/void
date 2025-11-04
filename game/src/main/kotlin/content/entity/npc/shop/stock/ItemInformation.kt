package content.entity.npc.shop.stock

import content.entity.npc.shop.shopInventory
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.client.ui.open

class ItemInformation : Script {

    init {
        interfaceOption("Info", id = "shop:*") {
            val sample = it.component == "sample"
            val actualIndex = it.itemSlot / (if (sample) 4 else 6)
            val inventory = shopInventory(sample)
            val item = inventory[actualIndex]
            set("info_sample", sample)
            set("info_index", actualIndex)
            val price = when {
                sample -> -1
                item.amount < 1 -> item.amount
                else -> Price.getPrice(this, item.id, actualIndex, item.amount)
            }
            ItemInfo.showInfo(this, item, price)
        }

        interfaceOption("Close", "item_info:exit") {
            when (menu) {
                "shop" -> {
                    open("shop_side")
                    interfaceOptions.send("shop_side", "inventory")
                }
                "grand_exchange" -> close("item_info")
            }
        }

        slotChanged { (inventory, idx, item) ->
            if (interfaces.contains("item_info")) {
                sendScript("refresh_item_info")
            }
            if (!contains("info_sample")) {
                return@slotChanged
            }
            val shop: String = get("shop") ?: return@slotChanged
            val index: Int = get("info_index") ?: return@slotChanged
            if (inventory == shop && idx == index) {
                set("item_info_price", if (item.amount == 0) 0 else Price.getPrice(this, item.id, index, item.amount))
            }
        }
    }

    /**
     * The item information side panel which shows a shop items requirements, stats and price
     */
}
