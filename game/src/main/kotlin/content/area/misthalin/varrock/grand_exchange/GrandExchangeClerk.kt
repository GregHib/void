package content.area.misthalin.varrock.grand_exchange

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.ChoiceBuilder
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.quest.questCompleted
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.npcApproach
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.event.Script
@Script
class GrandExchangeClerk {

    init {
        npcApproach("Talk-to", "grand_exchange_clerk*") {
            approachRange(2)
            if (Settings["grandExchange.tutorial.required", false] && !player.questCompleted("grand_exchange_tutorial")) {
                npc<Talk>("Excuse me, ${if (player.male) "sir" else "madam"}, but may I ask you to speak with Brugsen Bursen or the Gand Exchange Tutor near the entrance for a lesson. Brugsen will give an interesting, complete lesson on the Grand Exchange and the Tutor will give a smaller, plain lesson.")
                return@npcApproach
            }
            npc<Happy>("Welcome to the Grand Exchange. Would you like to trade now, or exchange item sets?")
            choice {
                option<Quiz>("How do I use the Grand Exchange?") {
                    npc<Happy>("My colleague and I can let you set up trade offers. You can offer to Sell items or Buy items.")
                    npc<Talk>("When you want to sell something, you give us the items and tell us how much money you want for them.")
                    npc<RollEyes>("We'll look for someone who wants to buy those items at your price, and we'll perform the trade. You can then collect the cash here, or at any bank.")
                    npc<Talk>("When you want to buy something, you tell us what you want, and give us the cash you're willing to spend on it.")
                    npc<RollEyes>("We'll look for someone who's selling those items at your price, and we'll perform the trade. You can then collect the items here, or at any bank, along with any left-over cash.")
                    npc<Happy>("Sometimes it takes a while to find a matching trade offer. If you change your mind, we'll let you cancel your trade offer, and we'll return your unused items and cash.")
                    npc<Quiz>("That's all the essential information you need to get started. Would you like to trade now, or exchange item sets?")
                    choice {
                        tradeOffers()
                        history()
                        itemSets()
                        bye()
                    }
                }
                tradeOffers()
                history()
                itemSets()
                bye()
            }
        }

        npcApproach("Exchange", "grand_exchange_clerk*") {
            approachRange(2)
            player.open("grand_exchange")
        }

        npcApproach("History", "grand_exchange_clerk*") {
            approachRange(2)
            player.open("exchange_history")
        }

        npcApproach("Sets", "grand_exchange_clerk*") {
            approachRange(2)
            player.open("exchange_item_sets")
        }

    }

    fun ChoiceBuilder<NPCOption<Player>>.tradeOffers() {
        option<Happy>("I'd like to set up trade offers please.") {
            player.open("grand_exchange")
        }
    }
    
    fun ChoiceBuilder<NPCOption<Player>>.history() {
        option<Quiz>("Can I see a history of my offers?") {
            npc<Talk>("If that is your wish.")
            player.open("exchange_history")
        }
    }
    
    fun ChoiceBuilder<NPCOption<Player>>.itemSets() {
        option<Quiz>("Can you help me with item sets?") {
            player.open("exchange_item_sets")
        }
    }
    
    fun ChoiceBuilder<NPCOption<Player>>.bye() {
        option<Upset>("I'm fine, thanks.")
    }
    
}
