package content.area.wilderness.bandit_camp

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player

class FatTony : Script {

    init {
        npcOperate("Talk-to", "fat_tony") { (target) ->
            dialogue(target.def["shop"])
        }
    }

    private suspend fun Player.dialogue(shop: String) {
        npc<Angry>("Go away I'm very busy.")
        choice {
            option<Neutral>("Sorry to disturb you.")
            option<Quiz>("What are you busy doing?") {
                npc<Neutral>("I'm cooking pizzas for the people in this camp.")
                npc<Angry>("Not that these louts appreciate my gourmet cooking!")
                busyChoices(shop)
            }
            option<Quiz>("Have you anything to sell?") {
                npc<Neutral>("Well, I guess I can sell you some half-made pizzas.")
                openShop(shop)
            }
        }
    }

    private suspend fun Player.busyChoices(shop: String) {
        choice {
            option<Quiz>("So what is a gourmet chef doing cooking for bandits?") {
                npc<Sad>("Well I'm an outlaw. I was accused of giving the king food poisoning! The thought of it! I think he just drank too much wine that night.")
                npc<Sad>("I had to flee the kingdom of Misthalin! The bandits give me refuge here as long as I cook for them.")
                afterStoryChoices(shop)
            }
            option<Quiz>("Can I have some pizza too?") {
                pizzaChoices(shop)
            }
            option<Neutral>("Okay, I'll leave you to it.")
        }
    }

    private suspend fun Player.afterStoryChoices(shop: String) {
        choice {
            option<Quiz>("Can I have some pizza too?") {
                pizzaChoices(shop)
            }
            option<Neutral>("Okay, I'll leave you to it.")
        }
    }

    private suspend fun Player.pizzaChoices(shop: String) {
        npc<Neutral>("Well this pizza is really meant to be for the bandits. I guess I could sell you some pizza bases though.")
        choice {
            option("Yes, Okay.") {
                openShop(shop)
            }
            option<Neutral>("Oh if I have to pay I don't want any.")
        }
    }
}
