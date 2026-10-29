package content.area.misthalin.zanaris

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import world.gregs.voidps.engine.Script

class GroveGatekeeper : Script {
    init {
        // https://runescape.wiki/w/Transcript:Gatekeeper#Near_the_entrance_to_the_Tanglefoot's_lair
        npcOperate("Talk-to", "gatekeeper_2_2") {
            if (questCompleted("fairy_tale_i")) {
                npc<Quiz>("Hello, did you just come from the market gates? Are my brothers doing their job well? I do worry that they'll start letting people into the market for free without me being there to supervise them!")
                return@npcOperate
            }
            player<Quiz>("What are you doing all the way over here? Shouldn't you be at the market door?")
            npc<Neutral>("Yes, that's my usual spot, but the Godfather said I should be here, so here I am.")
        }
    }
}
