package content.area.asgarnia.rimmington

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class BrianRimmington : Script {
    init {
        npcOperate("Talk-to", "brian_rimmington") { (target) ->
            npc<Happy>("Would you like to buy some archery equipment?")
            choice {
                option<Neutral>("No thanks, I've got all the archery equipment I need.") {
                    npc<Happy>("Okay. Fare well on your travels.")
                }
                option("Let's see what you've got, then.") {
                    openShop(target.def["shop"])
                }
            }
        }
    }
}
