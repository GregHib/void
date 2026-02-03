package content.area.misthalin.varrock.champions_guild

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class Valaine : Script {
    init {
        npcOperate("Talk-to", "valaine") { (target) ->
            npc<Happy>("Hello there. Want to have a look at what we're selling today?")
            choice {
                option("Yes please.") {
                    openShop(target.def["shop", ""])
                }
                option<Neutral>("No thank you.")
            }
        }
    }
}
