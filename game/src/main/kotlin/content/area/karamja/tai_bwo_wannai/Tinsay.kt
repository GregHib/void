package content.area.karamja.tai_bwo_wannai

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class Tinsay : Script {
    init {
        // https://runescape.wiki/w/Transcript:Tinsay#After_Tai_Bwo_Wannai_Trio
        npcOperate("Talk-to", "tinsay_tai_bwo_wannai_2") {
            player<Neutral>("Hello, Tinsay.")
            npc<Happy>("Why hello there! What are you doing back here?")
            player<Neutral>("Just passing through.")
        }
    }
}
