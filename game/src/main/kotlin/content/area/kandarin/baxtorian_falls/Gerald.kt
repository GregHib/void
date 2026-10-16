package content.area.kandarin.baxtorian_falls

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.quest
import world.gregs.voidps.engine.Script

class Gerald : Script {
    init {
        npcOperate("Talk-to", "gerald_baxtorian_falls") {
            if (quest("waterfall_quest") == "found_hudon") {
                npc<Shock>("Blimey! Where did you come from? Not another one of those treasure hunters are you?")
                player<Neutral>("Treasure hunters?")
                npc<Neutral>("They say there's treasure hidden within the waterfall, left behind by the old elven king. Not that anyone's ever found anything.")
                player<Neutral>("Interesting, is there somewhere I can learn more about this?")
                npc<Neutral>("You could ask Hadley the tourist guide. He'll be in this building just here.")
                return@npcOperate
            }
            player<Neutral>("Hello.")
            when (quest("waterfall_quest")) {
                "started" -> {
                    npc<Neutral>("Hello there.")
                    player<Neutral>("Have you seen a small boy?")
                    npc<Neutral>("Nope, plenty of small fish though.")
                }
                else -> {
                    npc<Neutral>("Hello traveller. Are you here to fish or to hunt for treasure?")
                    player<Neutral>("Why do you say that?")
                    npc<Neutral>("Adventurers pass through here every week, they never find anything though.")
                    player<Neutral>("What treasure are they looking for?")
                    npc<Neutral>("They say there's treasure hidden within the waterfall, left behind by the old elven king. Not that anyone's ever found anything.")
                    player<Neutral>("Interesting, is there somewhere I can learn more about this?")
                    npc<Neutral>("You could ask Hadley the tourist guide. He'll be in this building just here.")
                }
            }
        }
    }
}
