package content.area.karamja.tai_bwo_wannai

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class Tamayu : Script {
    init {
        // https://runescape.wiki/w/Transcript:Tamayu#After_Tai_Bwo_Wannai_Trio
        // Registered on the village spawn as tamayu_karamja_2 is also his pre-quest form in the jungle
        npcOperate("Talk-to", "tamayu_tai_bwo_wannai") {
            player<Neutral>("Hello, Tamayu, first son of Timfraku.")
            npc<Quiz>("Stranger, why you have returned?")
            player<Neutral>("I'm just passing through.")
            npc<Neutral>("Using your new found poison I have been creating spears. Would you like to buy some?")
            choice {
                option<Neutral>("Yes.") {
                    openShop("tamayus_spear_stall")
                }
                option<Neutral>("No.") {
                    npc<Neutral>("As you wish, stranger.")
                }
            }
        }
    }
}
