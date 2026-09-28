package content.area.asgarnia.dwarven_mines

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player

class CartConductor : Script {

    init {
        npcOperate("Talk-to", "cart_conductor") {
            menu()
        }
    }

    private suspend fun Player.menu() {
        choice("Select an Option") {
            option<Quiz>("Who are you?") {
                npc<Neutral>("I'm an employee of the Keldagrim carts. I make sure the carts in this area run on time and that people pay their fares.")
                menu()
            }
            option<Quiz>("Where can you take me?") {
                if (questCompleted("the_giant_dwarf")) {
                    npc<Neutral>("This track leads right up to Keldagrim. Only stop.")
                } else {
                    npc<Neutral>("I don't think I'm allowed to take you into the city of Keldagrim, human. Perhaps, when you find another way into the city and talk to someone of significance there, you will be allowed to.")
                }
                menu()
            }
            option<Quiz>("I'd like to buy a ticket.") {
                npc<Neutral>("You don't need a ticket. The Consortium has decided to grant free mine cart travel to all humans.")
            }
            option<Neutral>("I have to go.") {
                npc<Happy>("Just remember, wherever you go, you go there faster through Keldagrim carts.")
            }
        }
    }
}
