package content.area.kandarin.tree_gnome_stronghold

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.event.Script

@Script
class HeckelFunch {

    init {
        npcOperate("Talk-to", "gnome_heckelfunch") {
            player<Neutral>("Hello there.")
            npc<Happy>("Good day to you my friend, a beautiful one at that. Would you like some groceries? I have all sorts. Alcohol also, if you're partial to a drink.")
            choice {
                option<Neutral>("No thank you.") {
                    npc<Neutral>("Ahh well, all the best to you.")
                }
                option<Neutral>("I'll have a look.") {
                    npc<Happy>("There's a good human.")
                    player.openShop("funchs_fine_groceries")
                }
            }
        }
    }
}
