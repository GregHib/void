package content.area.misthalin.varrock

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class HorvikArmourShop : Script {

    init {
        npcOperate("Talk-to", "horvik") {
            npc<Happy>("Hello, do you need any help?")
            choice {
                option<Neutral>("Yes, please!") {
                    openShop("horviks_armour_shop")
                }
                option<Neutral>("No thanks. I'm just looking around.") {
                    npc<Happy>("Well, come and see me if you're ever in need of armour!")
                }
            }
        }
    }
}
