package content.area.fremennik_province.pirates_cove

import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.move.tele

class LokarSearunner : Script {
    init {
        npcOperate("Talk-to", "lokar_searunner_after") {
            if (tile in Areas["pirates_cove"]) {
                player<Neutral>("Hello again Lokar.")
                val title = "Dalkar Drapare"
                npc<Happy>("Hi again $title! What can I do for you?")
                choice {
                    option("Can you take me back to Rellekka?") {
                        player<Quiz>("Can you take me back to Rellekka please?")
                        npc<Neutral>("Hey, if you want to go back to loserville with all the losers, who am I to stop you?")
                        tele(2621, 3686)
                    }
                    option("Nothing thanks.") {
                        player<Happy>("Nothing thanks! I just saw you here and thought I'd say hello!")
                        npc<Happy>("Hey, I knew you seemed cool when I met you $title!")
                    }
                }
            } else {
                player<Quiz>("Hi Lokar, can you take me back to your ship?")
                npc<Confused>("Sheesh, make your mind up pal, I'm not a taxi service!")
                choice {
                    option("Go now.") {
                        tele(2213, 3794)
                    }
                    option("Don't go.") {
                        player<Neutral>("Actually, I've changed my mind. Again. I don't want to go.")
                        npc<Confused>("You are possibly the most indecisive person I have ever met...")
                        player<Neutral>("Well, 'bye then.")
                    }
                }
            }
        }

        npcOperate("Travel", "lokar_searunner_after") {
            if (tile in Areas["pirates_cove"]) {
                tele(2621, 3686)
            } else {
                tele(2213, 3794)
            }
        }
    }
}
