package content.area.asgarnia.port_sarim

import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.male

class CapnHand : Script {

    init {
        npcOperate("Talk-to", "capn_hand") {
            menu()
        }
    }

    private suspend fun Player.menu(greet: Boolean = true) {
        if (greet) {
            npc<Laugh>("Arrr!")
        }
        choice("What would you like to say?") {
            option<Laugh>("Arrr!") {
                npc<Laugh>("Arrr!")
                menu(greet = false)
            }
            option<Quiz>("You don't look very swashbuckling in there.") {
                npc<Neutral>("Aye, that be true. I were a famous pirate! They called me the terror of the seas and the scourge of the...the...other seas!")
                npc<Neutral>("But now I be stuck in a poxy cell with a scurvy dog who's kicked the bucket! They can't find enough cells for us all, so we have to share this'un and take turns using the bed!")
                player<Quiz>("So, do you think crime pays?")
                npc<Neutral>("Crime? Listen, young ${if (male) "laddie" else "lassie"}, I've spent nearly as much time handin' out loot to people as I ever spent stealing it in the first place. It's a crime that they locked me up!")
                menu(greet = false)
            }
            option<Neutral>("I'm leaving. Don't go anywhere.") {
                npc<Neutral>("Come again soon, young ${if (male) "lad" else "lass"}. This mangy cur in here with me isn't much use fer conversation, on account of how he hasn't got a 'Talk-to' option.")
            }
        }
    }
}
