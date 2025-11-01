package content.area.misthalin.lumbridge.roddecks_house

import content.entity.player.dialogue.Chuckle
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.RollEyes
import content.entity.player.dialogue.type.ChoiceBuilder2
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
                    npc<Neutral>("Apart from that, I'm just an elderly gentleman of Lumbridge, and this is my house.")
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

    fun ChoiceBuilder2.anyAdvice(): Unit = option<Quiz>("Can you offer me any advice?") {
        npc<Chuckle>("Advice? Certainly, certainly! Click my Advisor button whenever you have a question.")
    }

    fun ChoiceBuilder2.noThanks(): Unit = option<RollEyes>("Nothing, thanks.") {
    }
}
