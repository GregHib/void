package content.area.asgarnia.port_sarim

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class Betty : Script {
    init {
        npcOperate("Talk-to", "betty_port_sarim") { (target) ->
            npc<Happy>("Hello there. Welcome to my magic emporium.")
            choice {
                option<Quiz>("Can I see your wares?") {
                    npc<Happy>("Of course.")
                    openShop(target.def["shop"])
                }
                option<Neutral>("Sorry, I'm not into magic.") {
                    npc<Happy>("Well, if you see anyone who is, please send them my way.")
                }
            }
        }
    }
}
