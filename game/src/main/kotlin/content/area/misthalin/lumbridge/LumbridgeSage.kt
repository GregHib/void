package content.area.misthalin.lumbridge

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Idle
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class LumbridgeSage : Script {

    init {
        npcOperate("Talk-to", "lumbridge_sage") {
            npc<Happy>("Greetings, adventurer. How may I help you?")
            choice {
                whoSage()
                castleLum()
                option<Idle>("I'm fine for now, thanks.")
            }
        }
    }

    fun ChoiceOption.whoSage(): Unit = option<Quiz> ("Who are you?") {
        npc<Idle>("I am Phileas, the Lumbridge Sage. In times past, people came from all around to ask me for advice. My renown seems to have diminished somewhat in recent years, though. Can I help you with anything?")
        set("sage_advice_task", true)
        choice {
            castleLum()
            goodBye()
        }
    }

    fun ChoiceOption.castleLum(): Unit = option<Quiz>("Tell me about the town of Lumbridge.") {
        npc<Idle>("Lumbridge is one of the older towns in the human-controlled kingdoms. It was founded over two hundred years ago towards the end of the Fourth Age. It's called Lumbridge because of this bridge built over the")
        npc<Idle>("River Lum. The town is governed by Duke Horacio, who is a good friend of our monarch, King Roald of Misthalin.")
        set("sage_advice_task", true)
        choice {
            whoSage()
            goodBye()
        }
    }

    fun ChoiceOption.goodBye(): Unit = option<Happy>("Goodbye.") {
        npc<Happy>("Good adventuring, traveller.")
    }
}
