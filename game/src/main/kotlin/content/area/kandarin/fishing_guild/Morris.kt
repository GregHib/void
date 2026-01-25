package content.area.kandarin.fishing_guild

import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class Morris : Script {
    init {
        npcOperate("Talk-to", "morris") {
            player<Confused>("What are you sitting around here for?")
            npc<Neutral>("I'm making sure only those with a competition pass enter the fishing contest.")
        }
    }
}