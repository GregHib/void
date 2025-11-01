package content.skill.summoning

import content.entity.npc.shop.openShop
import world.gregs.voidps.engine.Script

class WishingWell : Script {

    init {
        objectOperate("Make-wish", "wishing_well") {
            openShop("summoning_supplies")
        }
    }
}
