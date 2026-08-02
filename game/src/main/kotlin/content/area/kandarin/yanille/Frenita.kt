package content.area.kandarin.yanille

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class Frenita : Script {
    init {
        npcOperate("Talk-to", "frenita") { (target) ->
            npc<Happy>("Would you like to buy some cooking equipment?")
            choice {
                option("Yes please.") {
                    openShop(target.def["shop"])
                }
                option<Neutral>("No thank you.")
            }
        }
    }
}