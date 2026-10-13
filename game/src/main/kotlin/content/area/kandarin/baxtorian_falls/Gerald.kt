package content.area.kandarin.baxtorian_falls

import content.entity.player.dialogue.Idle
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.quest
import world.gregs.voidps.engine.Script

class Gerald : Script {
    init {
        npcOperate("Talk-to", "gerald_baxtorian_falls") {
            if (quest("waterfall_quest") == "found_hudon") {
                npc<Idle>("Blimey! Where did you come from? Not another one of those treasure hunters are you?")
                player<Idle>("Treasure hunters?")
                npc<Idle>("They say there's treasure hidden within the waterfall, left behind by the old elven king. Not that anyone's ever found anything.")
                player<Idle>("Interesting, is there somewhere I can learn more about this?")
                npc<Idle>("You could ask Hadley the tourist guide. He'll be in this building just here.")
                return@npcOperate
            }
            player<Idle>("Hello.")
            when (quest("waterfall_quest")) {
                "unstarted", "started" -> {
                    npc<Idle>("Hello there.")
                    player<Idle>("Have you seen a small boy?")
                    npc<Idle>("Nope, plenty of small fish though.")
                }
                else -> {
                    npc<Idle>("Hello traveller. Are you here to fish or to hunt for treasure?")
                    player<Idle>("Why do you say that?")
                    npc<Idle>("Adventurers pass through here every week, they never find anything though.")
                    player<Idle>("What treasure are they looking for?")
                    npc<Idle>("They say there's treasure hidden within the waterfall, left behind by the old elven king. Not that anyone's ever found anything.")
                    player<Idle>("Interesting, is there somewhere I can learn more about this?")
                    npc<Idle>("You could ask Hadley the tourist guide. He'll be in this building just here.")
                }
            }
        }
    }
}
