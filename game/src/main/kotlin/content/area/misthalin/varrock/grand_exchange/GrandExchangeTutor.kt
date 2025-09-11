package content.area.misthalin.varrock.grand_exchange

import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.event.Script

@Script
class GrandExchangeTutor {

    init {
        npcOperate("Talk-to", "grand_exchange_tutor") {
            npc<Talk>("How can I help?")
            choice {
                option<Talk>("Can you teach me about the Grand Exchange again?") {
                    //            https://www.youtube.com/watch?v=g1estdWyrWU
                    npc<Talk>("Of course.")
                    npc<Talk>("The building you see here is the Grand Exchange. You can simply tell us what you want to buy or sell and for how much, and we'll pair you up with another player and make the trade for you!")
                    npc<Talk>("Buying and selling is done in a very similar way. Let me describe it in five steps.")
                    npc<Talk>("<maroon>Step 1</maroon>: You decide what to buy or sell and come here with the items to sell or the money to buy with.")
                    npc<Talk>("<maroon>Step 2</maroon>: Speak with one of the clerks, behind the desk in the middle of the building and they will guide you through placing the bid and the finer details of what you are looking for.")
                    npc<Talk>("<maroon>Step 3</maroon>: The clerks will take the items or money off you and look for someone to complete the trade.")
                    npc<Talk>("<maroon>Step 4</maroon>: You then need to wait perhaps a matter of moments or maybe days until someone is looking for what you have offered.")
                    npc<Talk>("<maroon>Step 5</maroon>: When the trade is complete, we will let you know with a message and you can pick up your winnings by talking to the clerks or by visiting any banker in ${Settings["server.name"]}.")
                    player["grand_exchange_tutorial"] = "completed"
                    npc<Talk>("There's a lot more information about the Grand Exchange, all of which you can find out from Brugsen Bursen, the guy with the megaphone. I would suggest you speak to him to fully get to grips with the Grand Exchange. Good luck!")
                }
                option<Talk>("Where can I find out more info?") {
                    npc<Talk>("Go and speak to Brugsen who's standing over there, closer to the building. He'll help you out.")
                }
                option<Talk>("I'm okay thanks.") {
                    npc<Talk>("Fair enough.")
                }
            }
        }
    }
}
