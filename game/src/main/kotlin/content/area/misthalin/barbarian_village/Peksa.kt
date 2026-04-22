package content.area.misthalin.barbarian_village

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import world.gregs.voidps.engine.Script

class Peksa : Script {

    // Both Rs3 / OSRS had same dialogue
    init {
        npcOperate("Talk-to", "peksa_barbarian_village") {
            npc<Quiz>("Are you interested in buying or selling a helmet?")
            choice {
                option("I could be, yes") {
                    openShop("peksas_helmet_shop")
                }
                option<Neutral>("No, I'll pass on that.") {
                    npc<Neutral>("Well, come back if you change your mind.")
                }
            }
        }
    }
}
