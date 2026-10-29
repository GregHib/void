package content.area.karamja.tai_bwo_wannai

import content.entity.npc.shop.openShop
import world.gregs.voidps.engine.Script

class Tiadeche : Script {
    init {
        npcOperate("Trade", "tiadeche*") {
            openShop("tiadeches_karambwan_stall")
        }
    }
}
