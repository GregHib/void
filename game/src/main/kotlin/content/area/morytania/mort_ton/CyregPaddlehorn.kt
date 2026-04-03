package content.area.morytania.mort_ton

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import world.gregs.voidps.engine.Script

class CyregPaddlehorn : Script {
    init {
        npcOperate("Talk-to", "cyreg_paddlehorn") {
            if (questCompleted("in_search_of_the_myreque")) {
                player<Neutral>("Thanks for your help in finding the Myreque.")
                npc<Sad>("Well, I'd like to say that you're welcome. But I heard that you led Vanstrom straight to their hideout and he killed Sani and Harold. I feel so guilty.")
                player<Sad>("Hmm, yeah, I know how you feel.")
            } else {
                npc<Sad>("Hello there friend. I'm afraid all boat rides are out of service due to the high ghast activity in Mort Myre.")
            }
        }
    }
}