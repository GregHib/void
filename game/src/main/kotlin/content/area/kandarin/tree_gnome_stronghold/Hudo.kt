package content.area.kandarin.tree_gnome_stronghold

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.event.Script

@Script
class Hudo : Api {

    init {
        npcOperateDialogue("Talk-to", "gnome_hudo") {
            player<Neutral>("Hello there.")
            npc<Neutral>("Hello there traveller. Would you like some groceries? I have a large selection.")
            choice {
                option<Neutral>("No thank you.") {
                    npc<Neutral>("No problem.")
                }
                option<Neutral>("I'll have a look.") {
                    npc<Happy>("Great stuff.")
                    player.openShop("grand_tree_groceries")
                }
            }
        }
    }
}
