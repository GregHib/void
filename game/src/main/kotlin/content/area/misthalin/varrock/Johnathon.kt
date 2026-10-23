package content.area.misthalin.varrock

import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class Johnathon : Script {

    init {
        npcOperate("Talk-to", "johnathon") {
            npc<Sad>("I am so very tired... Leave me be... to rest...")
        }
    }
}
