package content.area.misthalin.varrock

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Idle
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Scared
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.quest.member.gertrudes_cat.GERTRUDES_CAT_STRING_NAME
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player

const val SHILOP_STRING_NAME = "shilop"

class Shilop : Script {
    init {
        npcOperate("Talk-to", SHILOP_STRING_NAME){
            when (quest(GERTRUDES_CAT_STRING_NAME)) {
                "completed" -> postQuest()
                "spoke_to_gertrude" -> lookingForInformationBranch()
                "found_the_boys" -> gertrudeWhereFluffs()
                else -> gertrudeUnstarted()
            }
        }
    }
}

private suspend fun Player.lookingForInformationBranch() {
    gertrudeLookingForInformation()
    choice {
        option<Angry>("Tell me sonny, or I will inform your mum you are a pair of criminals.") {
            npc<Scared>("W..wh..what?! Y..you wouldn't! Anyway, I'll deny it all and she'll be sure to believe me over some wandering killer like you.", largeHead = true)
            player<Angry>("I'm an upstanding citizen!")
            npc<Angry>("I'm her darling boy and you'd have to forget about her rewarding you. Hop it snitch.", largeHead = true)
            npc<Happy>(npcId = WILOUGH_STRING_NAME, "And I'm even more her favourite than he is!", largeHead = true)
            statement("You decide it's best not to aggravate the repulsive boys any more.")
        }
        option<Quiz>("What will make you tell me?") {
            gertrudeFluffsCost()
        }
        option<Idle>("Well never mind, it's Fluffs loss.") {
            gertrudeFluffsLoss()
        }
    }
}