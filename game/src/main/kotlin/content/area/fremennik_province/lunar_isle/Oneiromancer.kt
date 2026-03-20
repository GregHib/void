package content.area.fremennik_province.lunar_isle

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class Oneiromancer : Script {
    init {
        npcOperate("Talk-to", "oneiromancer") {
            npc<Quiz>("Hello there. What do you want to talk about?")
            choice {
                option("The state of the island.") {
                    player<Neutral>("Hi, how are things going?")
                    npc<Neutral>("Well hopefully a lot better now that you've initiated the calm between the Moon Clan and the Fremenniks. Remember, if you want to use our Lunar Spells at any time, pray at the altar beside me and you can modify")
                    npc<Neutral>("your knowledge!")
                }
                option("Cyrisus.") {
                    player<Neutral>("Hi.")
                    npc<Sad>("Hello there. I hear that Cyrisus is no longer with us.")
                    player<Sad>("He died fighting for a good cause. He'll be remembered as a great hero.")
                    npc<Sad>("I suppose the good do die young.")
                }
                option("Nothing.")
            }
        }
    }
}
