package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class IceTitan : Script {
    init {
        npcOperate("Interact", "ice_titan_familiar") {
            npc<Neutral>("It's too hot here.")
            player<Happy>("It's really not that hot. I think it's rather pleasant.")
            npc<Neutral>("Well, it's alright for some. Some of us don't like the heat. I burn easily - well, okay, melt.")
            player<Happy>("Well, at least I know where to get a nice cold drink if I need one.")
            npc<Neutral>("What was that?")
            player<Happy>("Nothing. Hehehehe")
        }
    }
}
