package content.skill.summoning

import content.entity.npc.shop.openShop
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.event.Script

@Script
class WishingWell {

    init {
        objectOperate("Make-wish", "wishing_well") {
            player.openShop("summoning_supplies")
        }
    }
}
