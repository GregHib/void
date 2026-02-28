package content.area.misthalin.draynor_village

import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class ShadyStranger : Script {
    init {
        npcOperate("Talk-to", "shady_stranger,suspicious_outsider") { (target) ->
            player<Quiz>("Hello there. What are you doing here?")
            npc<Shifty>("Oh, nothing much. I'd just heard Draynor was a nice place to visit.")
            player<Confused>("Fair enough.")
        }
    }
}