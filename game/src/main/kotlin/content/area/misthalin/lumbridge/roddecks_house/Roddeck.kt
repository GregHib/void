package content.area.misthalin.lumbridge.roddecks_house

import content.entity.player.dialogue.Bored
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Idle
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class Roddeck : Script {

    init {
        npcOperate("Talk-to", "roddeck") {
            npc<Happy>("Greetings! I am Roddeck. What can i do for you today?")
            choice("What would you like to say?") {
                option<Quiz>("Who are you?") {
                    npc<Happy>("My name is Roddeck, and I am the Advisor. Whenever people in Runescape are in need of advice, they click on the Advisor button to seek my help.")
                    npc<Idle>("Apart from that, I'm just an elderly gentleman of Lumbridge, and this is my house.")
                    npc<Quiz>("Now, was there anything else you wanted?")
                    choice {
                        anyAdvice()
                        noThanks()
                    }
                }
                anyAdvice()
                noThanks()
            }
        }
    }

    fun ChoiceOption.anyAdvice(): Unit = option<Quiz>("Can you offer me any advice?") {
        npc<Laugh>("Advice? Certainly, certainly! Click my Advisor button whenever you have a question.")
    }

    fun ChoiceOption.noThanks(): Unit = option<Bored>("Nothing, thanks.") {
    }
}
