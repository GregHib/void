package content.skill.summoning

import content.entity.npc.shop.openShop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.obj.objectOperate

class WishingWell : Script {

    init {
        objectOperate("Make-wish", "wishing_well") {
            player.openShop("summoning_supplies")
        }
    }
}
