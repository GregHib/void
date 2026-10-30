package content.area.asgarnia.rimmington

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Idle
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class Rommik : Script {

    init {
        npcOperate("Talk-to", "rommik") { (target) ->
            npc<Happy>("Would you like to buy some Crafting equipment?")
            choice("Select an Option") {
                option("Let's see what you've got then.") {
                    openShop(target.def["shop"])
                }
                option<Idle>("No thanks, I've got all the crafting equipment I need.") {
                    npc<Happy>("Okay. Fare well on your travels.")
                }
            }
        }
    }
}
