package content.area.kandarin.ardougne

import content.entity.npc.shop.openShop
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.type.equals

@Script
class ArdougneBaker {
    init {
        npcOperate("Trade", "baker_ardougne") {
            if (target.tile.equals(2669, 3310)) {
                player.openShop("ardougne_bakers_stall_east")
            } else {
                player.openShop("ardougne_bakers_stall_west")
            }
        }
    }
}
