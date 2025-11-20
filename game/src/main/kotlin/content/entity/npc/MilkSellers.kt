package content.entity.npc

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Pleased
import content.entity.player.dialogue.type.*
import world.gregs.voidps.engine.Script

class MilkSellers : Script {
    init {
        npcOperate("Talk-to", "milk_seller") {
            npc<Happy>("Would you like to buy some milk?")
            choice {
                option<Happy>("Sure.") {
                    openShop("the_milk_shop")
                }
                option<Pleased>("No, thanks.") {
                    npc<Happy>("If you change your mind, you know where we are.")
                }
            }
        }
    }
}
