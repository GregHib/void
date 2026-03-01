package content.area.misthalin.draynor_village

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class Olivia : Script {
    init {
        npcOperate("Talk-to", "olivia") { (target) ->
            npc<Happy>("Would you like to trade in seeds?")
            choice {
                option("Yes") {
                    openShop(target.def["shop"])
                }
                option("No") {
                    player<Neutral>("No, thanks.")
                }
                option<Quiz>("Where do I get rarer seeds from?") {
                    npc<Neutral>("The Master Farmers usually carry a few rare seeds around with them, although I don't know if they'd want to part with them for any price to be honest.")
                }
            }
        }
    }
}
