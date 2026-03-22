package content.area.asgarnia.entrana

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class Frincos : Script {
    init {
        npcOperate("Talk-to", "mazion") { (target) ->
            npc<Neutral>("Hello, how can I help you?")
            choice {
                option("What are you selling?") {
                    openShop(target.def["shop"])
                }
                option<Neutral>("You can't; I'm beyond help.")
                option<Neutral>("I'm okay, thank you.")
            }
        }
    }
}
