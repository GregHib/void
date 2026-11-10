package content.area.kandarin.sinclair_mansion

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.statement
import content.quest.questCompleted
import world.gregs.voidps.engine.Script

class SinclairFamily : Script {

    init {
        // https://runescape.wiki/w/Transcript:Murder_Mystery (Post-quest dialogue)
        npcOperate("Talk-to", "anna_2,carol_2,elizabeth_2") {
            if (!questCompleted("murder_mystery")) {
                statement("She is ignoring you.")
                return@npcOperate
            }
            npc<Neutral>("Apparently you aren't as stupid as you look.")
        }

        npcOperate("Talk-to", "bob_sinclair_mansion_2,david_2,frank_2") {
            if (!questCompleted("murder_mystery")) {
                statement("He is ignoring you.")
                return@npcOperate
            }
            npc<Neutral>("Apparently you aren't as stupid as you look.")
        }
    }
}
