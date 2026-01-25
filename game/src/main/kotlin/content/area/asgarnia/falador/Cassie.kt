package content.area.asgarnia.falador

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class Cassie : Script {

    init {
        npcOperate("Talk-to", "cassie") {
            npc<Neutral>("I buy and sell shields; do you want to trade?")
            choice {
                option("Yes please.") {
                    player<Neutral>("Yes please.")
                    openShop("cassies_shield_shop")
                }
                option("No thank you.") {
                    player<Neutral>("No thank you.")
                }
            }
        }
    }
}
