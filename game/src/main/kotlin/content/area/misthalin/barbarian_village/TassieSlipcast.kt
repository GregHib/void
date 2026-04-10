package content.area.misthalin.barbarian_village

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class TassieSlipcast : Script {
    init {
        npcOperate("Talk-to", "tassie_slipcast") {
            npc<Happy>("Please feel free to use the pottery wheel, I won't be using it all the time. Put your pots in the kiln when you've made one.")
            npc<Happy>("And make sure you tidy up after yourself!")
        }
    }
}