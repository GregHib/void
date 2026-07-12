package content.area.asgarnia.taverley

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class ThaeriskCemphier : Script {

    init {
        // Taken from RS3
        npcOperate("Talk-to", "thaerisk_cemphier_2") {
            player<Happy>("What do you do here?")
            npc<Neutral>("Ah, well! I'm keeping an eye on things, making sure nothing untoward happens.")
            player<Neutral>("That's reassuring.")
            npc<Neutral>("One day I hope it will be.")
        }
    }
}
