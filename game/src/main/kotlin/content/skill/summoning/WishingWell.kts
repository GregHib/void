package content.skill.summoning

import content.entity.npc.shop.openShop
import world.gregs.voidps.engine.entity.obj.objectOperate

objectOperate("Make-wish", "wishing_well") {
    player.openShop("summoning_supplies")
}
