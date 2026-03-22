package content.area.misthalin.zanaris

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class Irksol : Script {
    init {
        npcOperate("Talk-to", "irksol") { (target) ->
            npc<Neutral>("Selling ruby rings! The best deals on rings in over twenty four hundred planes of existence!")
            choice {
                option<Neutral>("I'm interested in these deals.") {
                    npc<Neutral>("Aha! A connoisseur! Check out these beauties!")
                    openShop(target.def["shop"])
                }
                option<Neutral>("No thanks, just browsing.") {
                    npc<Neutral>("Fair enough.")
                }
            }
        }
    }
}
