package content.area.misthalin.varrock

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class Dimintheis : Script {

    init {
        // https://runescape.wiki/w/Transcript:Dimintheis (after Defender of Varrock)
        npcOperate("Talk-to", "dimintheis_normal") {
            player<Happy>("That was some impressive zombie-killing you did at the palace.")
            npc<Happy>("Yes, that shield is very impressive. Things are going well for me now. I've been declared a hero of Varrock and King Roald has said he's going to make it a priority to restore some land to the Fitzharmon family estate.")
            player<Neutral>("About time too.")
            npc<Happy>("I'm looking forward to getting out of this hole, I can tell you.")
        }
    }
}
