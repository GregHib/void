package content.area.asgarnia.falador

import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class Jeff : Script {

    init {
        npcOperate("Talk-to", "jeff") {
            npc<Quiz>("Tell me, is the guard still watching us?")
            choice {
                option<Quiz>("Why would you care if there's a guard watching you?") {
                    npc<Shifty>("Oh, forget it.")
                }
            }
        }
    }
}
