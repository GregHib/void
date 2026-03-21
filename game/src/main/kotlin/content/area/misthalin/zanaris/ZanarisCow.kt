package content.area.misthalin.zanaris

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class ZanarisCow : Script {
    init {
        npcOperate("Talk-to", "cow_zanaris") {
            npc<Neutral>("Hmm, interesting, there appears to be a human trying to talk to me. Is it aware that cows can't talk I wonder? Maybe if I just ignore it, it might go away.")
        }
    }
}
