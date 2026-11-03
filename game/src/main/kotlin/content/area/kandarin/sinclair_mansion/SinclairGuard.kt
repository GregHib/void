package content.area.kandarin.sinclair_mansion

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import world.gregs.voidps.engine.Script

class SinclairGuard : Script {

    init {
        // https://runescape.wiki/w/Transcript:Murder_Mystery (Post-quest dialogue)
        npcOperate("Talk-to", "guard_sinclair_mansion_2") {
            if (!questCompleted("murder_mystery")) {
                return@npcOperate
            }
            npc<Neutral>("Excellent work on solving the murder! All of the guards I know are very impressed, and don't worry, we have the murderer under guard until they can be taken to trial.")
            player<Neutral>("Is there anything else I can do? Seems awfully quiet up at the house, considering their sibling has just been arrested.")
            npc<Neutral>("Nothing right now, we have it all under control.")
        }
    }
}
