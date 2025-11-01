package content.area.kandarin.yanille

import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import content.skill.runecrafting.EssenceMine
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message

class WizardDistentor : Script {

    init {
        npcOperate("Talk-to", "wizard_distentor") { (target) ->
            npc<Talk>("Welcome to the Magicians' Guild!")
            if (!questCompleted("rune_mysteries")) {
                return@npcOperate
            }
            player<Talk>("Hello there.")
            npc<Quiz>("What can I do for you?")
            choice {
                option<Talk>("Nothing thanks, I'm just looking around.") {
                    npc<Talk>("That's fine with me.")
                }
                option<Quiz>("Can you teleport me to the Rune Essence Mine?") {
                    EssenceMine.teleport(target, this)
                }
            }
        }

        npcOperate("Teleport", "wizard_distentor") { (target) ->
            if (questCompleted("rune_mysteries")) {
                EssenceMine.teleport(target, this)
            } else {
                message("You need to have completed the Rune Mysteries Quest to use this feature.")
            }
        }
    }
}
