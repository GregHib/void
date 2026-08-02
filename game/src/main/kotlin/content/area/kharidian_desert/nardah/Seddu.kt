package content.area.kharidian_desert.nardah

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class Seddu : Script {
    init {
        npcOperate("Talk-to", "seddu") { (target) ->
            npc<Happy>("I buy and sell adventurer's equipment, do you want to trade?")
            choice {
                option("Yes please.") {
                    openShop(target.def["shop"])
                }
                option<Neutral>("No thank you.")
            }
        }
    }
}
