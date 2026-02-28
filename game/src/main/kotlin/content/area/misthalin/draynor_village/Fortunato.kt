package content.area.misthalin.draynor_village

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class Fortunato : Script {
    init {
        npcOperate("Talk-to", "fortunato") { (target) ->
            npc<Neutral>("Can I help you at all?")
            choice {
                option<Quiz>("Yes, what are you selling?") {
                    openShop(target.def["shop"])
                }
                option("Not at the moment") {
                    player<Neutral>("Not at the moment.")
                    npc<Angry>("Then move along, you filthy ragamuffin, I have customers to serve!")
                }
            }
        }
    }
}