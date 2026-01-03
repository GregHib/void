package content.area.kandarin.tree_gnome_stronghold

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import world.gregs.voidps.engine.Script

class HeckelFunch : Script {

    init {
        npcOperate("Talk-to", "gnome_heckelfunch") {
            player<Idle>("Hello there.")
            npc<Happy>("Good day to you my friend, a beautiful one at that. Would you like some groceries? I have all sorts. Alcohol also, if you're partial to a drink.")
            choice {
                option<Idle>("No thank you.") {
                    npc<Idle>("Ahh well, all the best to you.")
                }
                option<Idle>("I'll have a look.") {
                    npc<Happy>("There's a good human.")
                    openShop("funchs_fine_groceries")
                }
            }
        }
    }
}
