package content.area.misthalin.draynor_village

import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class ShadyStranger : Script {
    init {
        npcOperate("Talk-to", "shady_stranger,shady_stranger_2,suspicious_outsider") { (target) ->
            player<Quiz>("Hello, what are you doing here?")
            npc<Shifty>("Err, nothing much, just in here on business. Heard Draynor's a nice place to visit. Also at the same place great for woodcutting.")
        }
    }
}
