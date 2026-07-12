package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class SwampTitan : Script {
    init {
        npcOperate("Interact", "swamp_titan_familiar") {
            npc<Neutral>("I'm alone, all alone I say.")
            player<Happy>("Oh, stop being so melodramatic.")
            npc<Neutral>("It's not easy being greenery...well, decomposing greenery.")
            player<Happy>("Surely, you're not the only swamp...thing in the world? What about the other swamp titans?")
            npc<Neutral>("They're not my friends...they pick on me...they're so mean...")
            player<Happy>("Why would they do that?")
            npc<Neutral>("They think I DON'T smell.")
            player<Happy>("Oh, yes. That is, er, mean...")
        }
    }
}
