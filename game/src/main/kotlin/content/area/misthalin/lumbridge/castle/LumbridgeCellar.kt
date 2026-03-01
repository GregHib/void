package content.area.misthalin.lumbridge.castle

import content.entity.npc.shop.openShop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.open

class LumbridgeCellar : Script {
    init {
        objectOperate("Bank", "culinaromancers_chest") {
            open("bank")
        }

        objectOperate("Buy-food", "culinaromancers_chest") {
            openShop("culinaromancers_chest_food_9")
        }

        objectOperate("Buy-items", "culinaromancers_chest") {
            openShop("culinaromancers_chest_9")
        }
    }
}
