package content.area.misthalin.varrock.champions_guild

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class Guildmaster : Script {
    init {
        npcOperate("Talk-to", "guildmaster") {
            npc<Neutral>("Greetings!")
            choice {
                option<Quiz>("What is this place?") {
                    npc<Neutral>("This is the Champions' Guild. Only adventurers who have proved themselves worthy by gaining influence from quests are allowed in here.")
                }
            }
        }
    }
}