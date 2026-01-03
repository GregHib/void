package content.area.kharidian_desert.al_kharid

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class Ranael : Script {

    init {
        npcOperate("Talk-to", "ranael") {
            npc<Idle>("Do you want to buy any armoured skirts? Designed especially for ladies who like to fight.")
            choice {
                option<Idle>("Yes please.") {
                    openShop("ranaels_super_skirt_store")
                }
                option<Idle>("No thank you, that's not my scene.")
            }
        }
    }
}
