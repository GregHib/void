package content.area.misthalin.varrock

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class Zaff : Script {
    init {
        npcOperate("Talk-to", "zaff") {
            npc<Happy>("Would you like to buy or sell some staffs?")
            choice {
                option<Neutral>("Yes, please!") {
                    openShop("zaffs_superior_staffs")
                }
                option<Neutral>("No, thank you.") {
                    npc<Happy>("Well, 'stick' your head in again if you change your mind.")
                    player<Neutral>("Huh, terrible pun! You just can't get the 'staff' these days!")
                }
            }
        }
    }
}
