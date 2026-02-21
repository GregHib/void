package content.area.misthalin.lumbridge

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class Hank : Script {
    init {
        npcOperate("Talk-to", "hank") {
            npc<Neutral>("Good day to you! Welcome to my fishing shop. Would you like to buy some fishing equipment or sell some fish?")
            choice {
                option("Can I see your fishing supplies?") {
                    openShop(it.target.def["shop"])
                }
                option("Farewell.")
            }
        }
    }
}
