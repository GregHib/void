package content.area.misthalin.varrock

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class Lowe : Script {
    init {
        npcOperate("Talk-to", "lowe") {
            npc<Happy>("Welcome to Lowe's Archery Emporium.<br>Do you want to see my wares?")
            choice {
                option<Neutral>("Yes, please.") {
                    openShop("lowes_archery_emporium")
                }
                option<Neutral>("No, I prefer to bash things close up.") {
                }
            }
        }
    }
}
