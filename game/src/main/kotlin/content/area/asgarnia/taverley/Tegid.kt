package content.area.asgarnia.taverley

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.*
import world.gregs.voidps.engine.Script

class Tegid : Script {

    init {
        // https://youtu.be/oJ95Ii-ayAk?si=TLrbORlXU1hr28su&t=230
        npcOperate("Talk-to", "tegid") {
            player<Happy>("So, you're doing laundry, eh?")
            npc<Neutral>("Yes. What is it to you?")
            player<Happy>("Nice day for it.")
            npc<Neutral>("Suppose it is.")
        }
    }
}
