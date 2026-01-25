package content.area.asgarnia.falador

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class Cassie : Script {

    init {
        npcOperate("Talk-to", "cassie") {
            npc<Happy>("I buy and sell shields; do you want to trade?")
            choice {
                option<Neutral>("Yes please.") {
                    openShop("cassies_shield_shop")
                }
                option<Neutral>("No thank you.") {
                }
            }
        }
    }
}
