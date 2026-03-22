package content.area.misthalin.zanaris

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class Jukat : Script {
    init {
        npcOperate("Talk-to", "jukat") { (target) ->
            npc<Neutral>("Dragon swords! Here, Dragon swords! Straight from Frenaskrae!")
            choice {
                option("Yes please.") {
                    openShop(target.def["shop"])
                }
                option<Neutral>("No thanks, I'm just browsing.")
            }
        }
    }
}
