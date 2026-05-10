package content.area.wilderness.daemonheim

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class Sonje : Script {
    init {
        npcOperate("Talk-to", "sonje") {
            player<Quiz>("Hello there, what do you do here?")
            npc<Sad>("Sorry, I'm busy and can't really talk right now. Go up to the castle and talk to the tutor there.")
            player<Neutral>("Okay, thanks. I'll leave you to it.")
        }
    }
}
