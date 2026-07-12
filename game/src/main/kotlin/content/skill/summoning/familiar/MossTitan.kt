package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script

class MossTitan : Script {
    init {
        npcOperate("Interact", "moss_titan_familiar") {
            npc<Neutral>("Oh, look! A bug!")
            player<Happy>("It's quite a large bug.")
            npc<Neutral>("He's so cute! I wanna keep him.")
            player<Happy>("Well, be careful.")
            npc<Neutral>("I'm gonna call him Buggie and I'm gonna keep him in a box.")
            player<Happy>("Don't get overexcited.")
            npc<Neutral>("I'm gonna feed him and we're gonna be so happy together!")
            statement("The Moss titan begins to bounce up and down.")
            npc<Neutral>("Aww...Buggie went squish.")
            player<Happy>("Sigh.")
        }
    }
}
