package content.area.morytania.canifis

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class Malak : Script {
    init {
        npcOperate("Talk-to", "malak") {
            npc<Angry>("Be lucky I have let you live, meat. Our deal is done, I wish no further dealing with you.")
        }
    }
}
