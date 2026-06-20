package content.area.asgarnia.taverley

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.*
import world.gregs.voidps.engine.Script

class Gaius : Script {

    init {
        npcOperate("Talk-to", "gaius") {
            npc<Happy>("Welcome to my two-handed sword shop.")
            choice {
                option("Let's trade.") {
                    openShop("gaiuss_two_handed_shop")
                }
                option<Neutral>("Thanks, but not today.")
            }
        }
    }
}
