package content.area.karamja.tai_bwo_wannai

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class Tiadeche : Script {
    init {
        npcOperate("Trade", "tiadeche*") {
            openShop("tiadeches_karambwan_stall")
        }

        // https://runescape.wiki/w/Transcript:Tiadeche#After_completing_Tai_Bwo_Wannai_Trio.
        npcOperate("Talk-to", "tiadeche_tai_bwo_wannai_2") {
            player<Neutral>("Hello, Tiadeche.")
            npc<Happy>("Hello, Bwana! Would you like to buy some Karambwan?")
            choice {
                option<Neutral>("Yes.") {
                    openShop("tiadeches_karambwan_stall")
                }
                option<Neutral>("No.") {
                    npc<Neutral>("Very well. Let me know if you change your mind.")
                }
            }
        }
    }
}
