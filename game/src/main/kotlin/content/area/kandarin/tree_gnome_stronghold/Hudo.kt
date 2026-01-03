package content.area.kandarin.tree_gnome_stronghold

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import world.gregs.voidps.engine.Script

class Hudo : Script {

    init {
        npcOperate("Talk-to", "gnome_hudo") {
            player<Idle>("Hello there.")
            npc<Idle>("Hello there traveller. Would you like some groceries? I have a large selection.")
            choice {
                option<Idle>("No thank you.") {
                    npc<Idle>("No problem.")
                }
                option<Idle>("I'll have a look.") {
                    npc<Happy>("Great stuff.")
                    openShop("grand_tree_groceries")
                }
            }
        }
    }
}
