package content.area.asgarnia.port_sarim

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class Gerrant : Script {
    init {
        npcOperate("Talk-to", "gerrant*") { (target) ->
            npc<Neutral>("Welcome! You can buy fishing equipment at my store. We'll also buy anything you catch off you.")
            choice {
                option<Neutral>("Let's see what you've got then.") {
                    openShop(target.def["shop"])
                }
                option<Neutral>("Sorry, I'm not interested.")
            }
        }
    }
}
