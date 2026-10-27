package content.quest.free.vampire_slayer

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player

class Morgan : Script {
    init {
        npcOperate("Talk-to", "morgan") { talk() }
    }

    private suspend fun Player.talk() {
        when (quest("vampire_slayer")) {
            "unstarted" -> {
                npc<Neutral>("Please help! A vampyre is terrorising Draynor Village.")
                player<Neutral>("Where can I find it?")
                npc<Neutral>("Count Draynor lives in the cellar of the manor to the north.")
                choice {
                    option("I'll help you.") { accept() }
                    option("Do you have any advice?") { accept() }
                    option("Vampyres sound too dangerous.") {
                        npc<Neutral>("Please come back if you change your mind.")
                    }
                }
            }
            "completed" -> npc<Happy>("Thank you for saving our village from Count Draynor!")
            else -> npc<Neutral>("Speak to Dr Harlow at the Blue Moon Inn in Varrock. Tell him Morgan sent you.")
        }
    }

    private suspend fun Player.accept() {
        npc<Neutral>("My friend Dr Harlow knows how to fight vampyres. Find him at the Blue Moon Inn in Varrock and tell him Morgan sent you.")
        set("vampire_slayer", "started")
    }
}
