package content.area.misthalin.varrock.museum

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class OrlandoSmith : Script {
    init {
        npcOperate("Talk-to", "orlando_smith") {
            // TODO
            npc<Happy>("Thanks for all your help, mate. Hope you enjoy the museum!")
        }
    }
}