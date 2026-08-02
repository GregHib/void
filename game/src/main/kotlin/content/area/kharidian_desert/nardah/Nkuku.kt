package content.area.kharidian_desert.nardah

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import world.gregs.voidps.engine.Script

class Nkuku : Script {
    init {
        npcOperate("Talk-to", "nkuku") { (target) ->
            if (questCompleted("spirits_of_the_elid")) {
                npc<Neutral>("It still shocks me that the spirits of the river would put such a nasty curse on us for something so small.")
                player<Neutral>("Well strange are the ways of ancient beings such as them.")
            } else {
                player<Neutral>("Good day to you.")
                npc<Neutral>("May Saradomin be with you.")
            }
        }
    }
}
