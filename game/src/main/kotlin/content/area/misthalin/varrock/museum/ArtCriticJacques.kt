package content.area.misthalin.varrock.museum

import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class ArtCriticJacques : Script {
    init {
        npcOperate("Talk-to", "art_critic_jacques") {
            player<Neutral>("Hello.")
            npc<Happy>("Ah, many greetings, and welcome to the Museum!")
            choice {
                option<Quiz>("Who are you?") {
                    npc<Happy>("My name is Jacques and I am the Museum's finest art critic!")
                    player<Quiz>("Looks to me like you're the only one.")
                    npc<Happy>("This is true. However, in the future, I will have my own entire floor of the Museum with paintings galore!")
                    player<Quiz>("Er... has that been approved, or are you just dreaming?")
                    npc<Neutral>("Ah, I see you're being witty, eh?")
                    player<Confused>("I am?")
                    npc<Happy>("Yes! You're poking fun at me! I shall have my gallery. You will see eventually. Then I shall be the one with the last laugh!")
                }
                option<Quiz>("What do you do here?") {
                    npc<Laugh>("I critique the art! See the beautiful colours and the masterful use of the brush to capture perfectly the royal presence...")
                }
            }
        }
    }
}
