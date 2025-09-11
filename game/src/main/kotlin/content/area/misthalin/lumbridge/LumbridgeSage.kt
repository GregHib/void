package content.area.misthalin.lumbridge

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.PlayerChoice
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.event.Script

@Script
class LumbridgeSage {

    init {
        npcOperate("Talk-to", "lumbridge_sage") {
            npc<Happy>("Greetings, adventurer. How may I help you?")
            choice {
                whoSage()
                castleLum()
                option<Neutral>("I'm fine for now, thanks.")
            }
        }
    }

    suspend fun PlayerChoice.whoSage(): Unit = option<Quiz> ("Who are you?") {
        npc<Neutral>("I am Phileas, the Lumbridge Sage. In times past, people came from all around to ask me for advice. My renown seems to have diminished somewhat in recent years, though. Can I help you with anything?")
        player["sage_advice_task"] = true
        choice {
            castleLum()
            goodBye()
        }
    }

    suspend fun PlayerChoice.castleLum(): Unit = option<Quiz>("Tell me about the town of Lumbridge.") {
        npc<Neutral>("Lumbridge is one of the older towns in the human-controlled kingdoms. It was founded over two hundred years ago towards the end of the Fourth Age. It's called Lumbridge because of this bridge built over the")
        npc<Neutral>("River Lum. The town is governed by Duke Horacio, who is a good friend of our monarch, King Roald of Misthalin.")
        player["sage_advice_task"] = true
        choice {
            whoSage()
            goodBye()
        }
    }

    suspend fun PlayerChoice.goodBye(): Unit = option<Happy>("Goodbye.") {
        npc<Happy>("Good adventuring, traveller.")
    }
}
