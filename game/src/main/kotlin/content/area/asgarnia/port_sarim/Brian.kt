package content.area.asgarnia.port_sarim

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class Brian : Script {
    init {
        npcOperate("Talk-to", "brian_port_sarim") { (target) ->
            choice {
                option<Neutral>("So, are you selling something?") {
                    npc<Neutral>("Yep, take a look at these great axes!")
                    openShop(target.def["shop"])
                }
                option<Neutral>("'Ello.") {
                    npc<Neutral>("'Ello!")
                }
            }
        }
    }
}
